package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.brown.cs.jmrs.collect.Functional;
import edu.brown.cs.jmrs.io.db.DbConn;
import edu.brown.cs.jmrs.io.db.DbReader;
import edu.brown.cs.jmrs.io.db.DbWriter;
import edu.brown.cs.jmrs.io.db.Insert;
import edu.brown.cs.jmrs.io.db.Query;
import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.web.Link;
import edu.brown.cs.jmrs.web.Page;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder;

/**
 * An edge finder for use in graph searching on WikiPages that uses a database
 * cache of edges to speed up operations.
 *
 * @author mcisler
 *
 */
public class CachingWikiLinkFinder extends WikiPageLinkFinder {
  private static final DbReader<Link> LINK_READER = new DbReader<>((rs) -> {
    // rs stores two urls; use Main cache for insides
    return new Link(new WikiPage(rs.getString(1), Main.WIKI_PAGE_DOC_CACHE),
        new WikiPage(rs.getString(2), Main.WIKI_PAGE_DOC_CACHE));
  }); // don't do prehash because there are so many possible pages

  private static final DbWriter<Link> LINK_WRITER =
      new DbWriter<>((ps, link) -> {
        ps.setString(1, link.getSource().url());
        ps.setString(2, link.getDestination().url());
      }, "CREATE TABLE IF NOT EXISTS links(" + "start TEXT," + "end TEXT,"
          + "index_time DATETIME DEFAULT CURRENT_TIMESTAMP,"
          + "PRIMARY KEY (start, end));"
          + "CREATE INDEX IF NOT EXISTS start_index ON links (start);");
  // +"start-links INT,"+"end-links INT"

  private static final double RE_CACHE_PROBABILITY = 0.1;

  private final CacherService cacherService;

  private final Query<Link>  lookup;
  private final Insert<Link> cacher;

  /**
   * Creates a CachingWikiEdgeFinder that caches (and hangs while doing so) all
   * found links on the spot.
   *
   * @param conn
   *          The conn to use to query/update the WikiPage and Link database.
   * @param filters
   *          A series of filters to ignore links by.
   * @throws SQLException
   *           If the required table could not be created.
   */
  public CachingWikiLinkFinder(DbConn conn, Filter... filters)
      throws SQLException {
    super(filters);
    lookup =
        conn.makeQuery("SELECT * FROM links WHERE start=?", LINK_READER, true);
    cacher =
        conn.makeInsert(
            "INSERT OR REPLACE INTO links (start, end) VALUES (?, ?)",
            LINK_WRITER);

    // nullify worker to signify to just do it on this thread
    cacherService = null;
  }

  /**
   * Creates a CachingWikiEdgeFinder with a series of worker threads to actually
   * send links to the database, for better responsiveness.
   *
   * @param conn
   *          The conn to use to query/update the WikiPage and Link database.
   * @param cacheWorkerExecutionPercentage
   *          The desired percentage of CPU time the DB caching worker threads
   *          use.
   * @param filters
   *          A series of filters to ignore links by.
   * @throws SQLException
   *           If the required table could not be created.
   */
  public CachingWikiLinkFinder(DbConn conn,
      double cacheWorkerExecutionPercentage, Filter... filters)
      throws SQLException {
    super(filters);
    lookup =
        conn.makeQuery("SELECT * FROM links WHERE start=?", LINK_READER, true);
    cacher =
        conn.makeInsert(
            "INSERT OR IGNORE INTO links (start, end) VALUES (?, ?)",
            LINK_WRITER);

    // create worker that does batch sizes estimated to take up the desired time
    cacherService = new CacherService(cacheWorkerExecutionPercentage);
  }

  @Override
  public Set<String> links(WikiPage page) throws IOException {
    // try database
    List<Link> links = lookup.query(page.url());
    if (links.isEmpty() || shouldReCache()) {
      // grab links using WikiPage link finder in normal way
      Set<String> urls = super.links(page);

      // and cache them as Links (deferring to other thread)
      addLinks(
          Functional.map(urls, (url) -> new Link(page, new WikiPage(url))));

      return urls;
    }
    return new HashSet<>(
        Functional.map(links, (link) -> link.getDestination().url()));
  }

  private boolean shouldReCache() {
    return Math.random() <= RE_CACHE_PROBABILITY;
  }

  @Override
  public Set<WikiPage> linkedPages(WikiPage page) throws IOException {
    // try database
    List<Link> links = lookup.query(page.url());
    if (links.isEmpty() || shouldReCache()) {
      // grab links using WikiPage link finder in normal way
      Set<WikiPage> pages = super.linkedPages(page);

      // and cache them as Links
      addLinks(Functional.map(pages, (dest) -> new Link(page, dest)));

      return pages;
    }
    return new HashSet<>(
        Functional.map(links, (link) -> (WikiPage) link.getDestination()));
  }

  @Override
  public Set<Link> edges(Page node) {
    List<Link> links = lookup.query(node.url());
    if (links.isEmpty() || shouldReCache()) {
      // grab edges using wikipage link finder in normal way
      Set<Link> edges = super.edges(node);

      // and cache them
      addLinks(edges);

      return edges;
    }
    return new HashSet<>(links);
  }

  @Override
  public Number edgeValue(Link edge) {
    // TODO? (This is actually REALLY HARD)
    return 1;
  }

  private void addLinks(Set<Link> links) {
    if (cacherService == null) {
      cacher.insertAll(links);
    } else {
      cacherService.addLinks(links);
    }
  }

  /***************************************************************************/
  /* MULTITHREADED DATABASE CACHING UTILITIES */
  /***************************************************************************/

  /**
   * A runnable to handle deferring of database storage. Allows for quick
   * responses to queries while also doing the heavy work of DB storage.
   *
   * Keeps a BlockingQueue of links to cache, and adds them to the database in
   * SQL batches. The batch size is determine dynamically to meet a desired
   * percentage of total execution time.
   *
   * @author mcisler
   *
   */
  class CacherWorker implements Runnable {
    private static final int    INITIAL_BATCH_SIZE = 50; // keep it low on
                                                         // startup
    private static final double BATCH_SIZES_TO_AVG = 5;
    private double              desiredExecutionTime;

    private final BlockingQueue<Link> linksToCache;
    private int                       curBatchSize;

    /**
     * Creates and starts a CacherWorker.
     *
     * @param linksToCache
     *          A collection of links to get links to be cached from.
     * @param desiredExecutionPercentage
     *          The desired time this CacherWorker should spend on each batch.
     */
    CacherWorker(BlockingQueue<Link> linksToCache,
        double desiredExecutionTime) {
      this.linksToCache = linksToCache;
      this.curBatchSize = INITIAL_BATCH_SIZE;
      this.desiredExecutionTime = desiredExecutionTime;
    }

    @Override
    public void run() {
      try {
        Set<Link> buffer = new HashSet<>(curBatchSize);
        for (int i = 0; i < curBatchSize; i++) {
          buffer.add(linksToCache.take());
        }
        Main.debugLog("\tCaching buffer of " + curBatchSize);
        long start = System.currentTimeMillis();
        cacher.insertAll(buffer);
        long duration = System.currentTimeMillis() - start;

        // adjust current value by deviation (ratio) off desired
        // average last ten values (weight the new one ony a bit and the old
        // (average) higher)
        double newBatchSize =
            (desiredExecutionTime / (double) duration) * curBatchSize;
        curBatchSize =
            (int) (newBatchSize * 1.0 / BATCH_SIZES_TO_AVG + curBatchSize
                * (BATCH_SIZES_TO_AVG - 1.0) / BATCH_SIZES_TO_AVG);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * An extension of a ScheduledThreadPoolExecutor for caching WikiPages with
   * multiple threads.
   *
   * @author mcisler
   *
   */
  class CacherService extends ScheduledThreadPoolExecutor {
    public static final int           MAX_THREAD_EXECUTE_PERIOD = 2000; // ms
    private final BlockingQueue<Link> linksToCache;

    /**
     * Creates a ExecutorService (Thread Pool) running several CacherWorkers.
     *
     * @param desiredExecutionPercentage
     *          The desired percentage of CPU time for each CacherWorker worker
     *          to use.
     * @param numThreads
     *          The number of threads to use in the ExecutorService.
     */
    CacherService(double desiredExecutionPercentage) {
      super(1); // 1 thread
      assert desiredExecutionPercentage > 0 && desiredExecutionPercentage < 1;

      linksToCache = new LinkedBlockingQueue<>();
      double desiredExecutionTime =
          desiredExecutionPercentage * MAX_THREAD_EXECUTE_PERIOD;

      Runnable worker = new CacherWorker(linksToCache, desiredExecutionTime);

      // schedule at staggered times
      scheduleWithFixedDelay(worker, 0,
          (long) (MAX_THREAD_EXECUTE_PERIOD - desiredExecutionTime),
          TimeUnit.MILLISECONDS);
    }

    /**
     * @param links
     *          Add these links to those to be cached.
     */
    void addLinks(Collection<Link> links) {
      linksToCache.addAll(links);
      Main.debugLog("\tAdded " + links.size() + " links for caching; now at "
          + linksToCache.size());
    }
  }
}
