package edu.brown.cs.jmrs.wikispeedia;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import edu.brown.cs.jmrs.collect.graph.EdgeFinder;
import edu.brown.cs.jmrs.io.db.DbConn;
import edu.brown.cs.jmrs.web.Link;
import edu.brown.cs.jmrs.web.Page;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * A basic wikipedia page scraper.
 *
 * @author mcisler
 *
 */
public class Scraper {

  private final DbConn conn;
  private final WikiPage startPage;
  private int depth;
  private EdgeFinder<Page, Link> edgeFinder;

  /**
   * @param wikiDbConn
   *          The DbConn of the wikipedia database.
   * @param startPage
   *          The WikiPage to start at.
   * @throws SQLException
   *           If the database could not be loaded or was malformed.
   */
  public Scraper(DbConn wikiDbConn, WikiPage startPage) throws SQLException {
    conn = wikiDbConn;
    this.startPage = startPage;
    depth = -1;
    edgeFinder = new CachingWikiEdgeFinder(wikiDbConn);
  }

  /**
   * @param depth
   *          The depth to go to in scraping.
   */
  public void setDepth(int depth) {
    this.depth = depth;
  }

  /**
   * Starts scraping using a breadth first approach. Stops if depth is not equal
   * to -1
   */
  public void start() {
    int curDepth = 0;
    WikiPage curStart = startPage;
    while (depth == -1 || curDepth < depth) {
      Set<Link> links = edgeFinder.edges(startPage);
      for (Link link : links) {
        // cache all edges of each
        edgeFinder.edges(link.getDestination());
      }
      // then go down one for next iteration
      curStart =
          (WikiPage) new ArrayList<>(links)
              .get((int) (Math.random() * links.size())).getDestination();
      curDepth++;
    }
  }
}
