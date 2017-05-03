package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class CachingWikiEdgeFinder extends WikiPageLinkFinder {
  // TODO: How to prehash?
  private static final DbReader<Link> LINK_READER = new DbReader<>((rs) -> {
    // rs stores two urls (TODO??); use Main cache for insides
    return new Link(new WikiPage(rs.getString(1), Main.WIKI_PAGE_DOC_CACHE),
        new WikiPage(rs.getString(2), Main.WIKI_PAGE_DOC_CACHE));
  });

  private static final DbWriter<Link> LINK_WRITER =
      new DbWriter<>((ps, link) -> {
        ps.setString(1, link.getSource().url());
        ps.setString(2, link.getDestination().url());
      }, "CREATE TABLE IF NOT EXISTS links(" + "start TEXT," + "end TEXT,"
          + "PRIMARY KEY (start);");

  private final Query<Link> lookup;
  private final Insert<Link> cacher; // TODO: Will this be here?

  /**
   * @param conn
   *          The conn to use to query/update the WikiPage and Link database.
   * @throws SQLException
   *           If the required table could not be created.
   */
  public CachingWikiEdgeFinder(DbConn conn) throws SQLException {
    lookup =
        conn.makeQuery("SELECT * FROM links WHERE start=?", LINK_READER, true);
    cacher = conn.makeInsert("UPDATE links SET start=?, end=?", LINK_WRITER);
  }

  @Override
  public Set<String> links(WikiPage page) throws IOException {
    // try database
    List<Link> links = lookup.query(page.url());
    if (links.isEmpty()) {
      // grab links using wikipage link finder in normal way
      return super.links(page);
    }
    return new HashSet<>(
        Functional.map(links, (link) -> link.getDestination().url()));
  }

  @Override
  public Set<WikiPage> linkedPages(WikiPage page) throws IOException {
    // try database
    List<Link> links = lookup.query(page.url());
    if (links.isEmpty()) {
      // grab links using wikipage link finder in normal way
      return super.linkedPages(page);
    }
    return new HashSet<>(
        Functional.map(links, (link) -> (WikiPage) link.getDestination()));
  }

  @Override
  public Set<Link> edges(Page node) {
    List<Link> links = lookup.query(node.url());
    if (links.isEmpty()) {
      // grab edges using wikipage link finder in normal way
      return super.edges(node);
    }
    return new HashSet<>(links);
  }

  @Override
  public Number edgeValue(Link edge) {
    // TODO? (This is actually REALLY HARD)
    return 1;
  }
}
