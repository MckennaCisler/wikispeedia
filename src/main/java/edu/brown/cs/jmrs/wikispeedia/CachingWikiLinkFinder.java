package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
  private static final int NUM_DB_CACHING_THREADS = 2;
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

  private final Executor cachingWorkers;

  private final Query<Link> lookup;
  private final Insert<Link> cacher;

  /**
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
            "INSERT OR IGNORE INTO links (start, end) VALUES (?, ?)",
            LINK_WRITER);
    cachingWorkers = Executors.newFixedThreadPool(NUM_DB_CACHING_THREADS);
  }

  @Override
  public Set<String> links(WikiPage page) throws IOException {
    // try database
    List<Link> links = lookup.query(page.url());
    if (links.isEmpty()) {
      // grab links using WikiPage link finder in normal way
      Set<String> urls = super.links(page);

      // and cache them as Links (deferring to other thread)
      cachingWorkers.execute(new CacherWorker(
          Functional.map(urls, (url) -> new Link(page, new WikiPage(url)))));

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
      cachingWorkers.execute(new CacherWorker(
          Functional.map(pages, (dest) -> new Link(page, dest))));

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
      cachingWorkers.execute(new CacherWorker(edges));

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
   * A simple runnable to enable deferring of database storage. Allows for quick
   * responses to queries while also doing the heavy work of DB storage.
   *
   * @author mcisler
   *
   */
  private class CacherWorker implements Runnable {
    private final Set<Link> linksToCache;

    CacherWorker(Set<Link> linksToCache) {
      this.linksToCache = linksToCache;
    }

    @Override
    public void run() {
      cacher.insertAll(linksToCache);
    }
  }
}
