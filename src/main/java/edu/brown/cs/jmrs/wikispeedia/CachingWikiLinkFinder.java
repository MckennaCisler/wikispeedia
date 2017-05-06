package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.BlockingArrayQueue;

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
  private static final int NUM_DB_CACHING_THREADS = 1;

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
          + "PRIMARY KEY (start, end));"); // + "start-links INT," + "end-links
                                           // INT"

  private final CacherWorker cacherWorker;

  private final Query<Link> lookup;
  private final Insert<Link> cacher;

  /**
   * @param conn
   *          The conn to use to query/update the WikiPage and Link database.
   * @param cacheWorkerExecutionPercentage
   *          The desired percentage of CPU time the DB caching worker thread
   *          uses.
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
    cacherWorker = new CacherWorker(cacheWorkerExecutionPercentage);
  }

  @Override
  public Set<String> links(WikiPage page) throws IOException {
    // try database
    List<Link> links = lookup.query(page.url());
    if (links.isEmpty()) {
      // grab links using WikiPage link finder in normal way
      Set<String> urls = super.links(page);

      // and cache them as Links (deferring to other thread)
      cacherWorker.addLinks(
          Functional.map(urls, (url) -> new Link(page, new WikiPage(url))));

      return urls;
    }
    return new HashSet<>(
        Functional.map(links, (link) -> link.getDestination().url()));
  }

  @Override
  public Set<WikiPage> linkedPages(WikiPage page) throws IOException {
    // try database
    List<Link> links = lookup.query(page.url());
    if (links.isEmpty()) {
      // grab links using WikiPage link finder in normal way
      Set<WikiPage> pages = super.linkedPages(page);

      // and cache them as Links
      cacherWorker
          .addLinks(Functional.map(pages, (dest) -> new Link(page, dest)));

      return pages;
    }
    return new HashSet<>(
        Functional.map(links, (link) -> (WikiPage) link.getDestination()));
  }

  @Override
  public Set<Link> edges(Page node) {
    List<Link> links = lookup.query(node.url());
    if (links.isEmpty()) {
      // grab edges using wikipage link finder in normal way
      Set<Link> edges = super.edges(node);

      // and cache them
      cacherWorker.addLinks(edges);

      return edges;
    }
    return new HashSet<>(links);
  }

  @Override
  public Number edgeValue(Link edge) {
    // TODO? (This is actually REALLY HARD)
    return 1;
  }

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
  private class CacherWorker implements Runnable {
    private static final int MAX_THREAD_EXECUTE_PERIOD = 2000; // ms
    private static final int INITIAL_BATCH_SIZE = 1000;
    private static final int BATCH_SIZES_TO_AVG = 5;
    private double desiredExecutionTime;

    private final BlockingQueue<Link> linksToCache;
    private int curBatchSize;

    /**
     * Creates and starts a CacherWorker.
     *
     * @param desiredExecutionPercentage
     *          The desired percentage of CPU time for this worker to use.
     */
    CacherWorker(double desiredExecutionPercentage) {
      this.linksToCache = new BlockingArrayQueue<>(); // growable
      this.curBatchSize = INITIAL_BATCH_SIZE;

      desiredExecutionTime =
          desiredExecutionPercentage * MAX_THREAD_EXECUTE_PERIOD;

      if (desiredExecutionPercentage >= 1) {
        ExecutorService workerExecutor = Executors.newSingleThreadExecutor();
        workerExecutor.execute(this);
      } else {
        ScheduledExecutorService workerExecutor =
            Executors.newSingleThreadScheduledExecutor();
        workerExecutor.scheduleWithFixedDelay(this, 0L,
            (long) (MAX_THREAD_EXECUTE_PERIOD - desiredExecutionTime),
            TimeUnit.MILLISECONDS);
      }
    }

    @Override
    public void run() {
      long start = System.currentTimeMillis();
      Set<Link> buffer = new HashSet<>(curBatchSize);
      linksToCache.drainTo(buffer, curBatchSize);
      cacher.insertAll(buffer);

      long duration = System.currentTimeMillis() - start;

      // adjust current value by deviation (ratio) off desired
      // average last ten values (weight the new one ony a bit and the old
      // (average) higher)
      double newBatchSize = (duration / desiredExecutionTime) * curBatchSize;
      curBatchSize =
          (int) newBatchSize * 1 / BATCH_SIZES_TO_AVG
              + curBatchSize * (BATCH_SIZES_TO_AVG - 1) / BATCH_SIZES_TO_AVG;
    }

    void addLinks(Collection<Link> links) {
      linksToCache.addAll(links);
    }
  }
}
