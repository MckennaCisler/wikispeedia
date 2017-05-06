package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.brown.cs.jmrs.io.db.DbConn;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder.Filter;

/**
 * A basic wikipedia page scraper.
 *
 * @author mcisler
 *
 */
public class Scraper {

  // underestimate
  private static final int     AVG_LINKS_PER_PAGE = 100;

  private final WikiPage       startPage;
  private int                  depth;
  private LinkFinder<WikiPage> linkFinder;

  /**
   * @param wikiDbConn
   *          The DbConn of the wikipedia database.
   * @param startPage
   *          The WikiPage to start at.
   * @param filters
   *          A series of filters to ignore links by.
   * @throws SQLException
   *           If the database could not be loaded or was malformed.
   */
  public Scraper(DbConn wikiDbConn, WikiPage startPage, Filter... filters)
      throws SQLException {
    this.startPage = startPage;
    depth = -1;
    // linkFinder = new CachingWikiLinkFinder(wikiDbConn, filters);
  }

  /**
   * @param depth
   *          The depth to go to in scraping.
   */
  public void setDepth(int depth) {
    this.depth = depth;
  }

  /**
   * Starts scraping by trying all children at a given level. Stops if depth is
   * not equal to -1
   */
  public void startBreadthFirstScrape() {
    int curDepth = 0;
    Set<WikiPage> searchLinks;
    Set<WikiPage> nextSearchLinks = new HashSet<>();

    try {
      searchLinks = linkFinder.linkedPages(startPage);
    } catch (IOException e) {
      throw new AssertionError(
          "Start page not reachable: " + e.getMessage(),
          e);
    }

    while (depth == -1 || curDepth < depth) {
      for (WikiPage page : searchLinks) {
        // add all edges to be searched (and cached) at the next round
        try {
          Set<WikiPage> linksOfPage = linkFinder.linkedPages(page);
          nextSearchLinks.addAll(linksOfPage);
          System.out.printf(
              String.format(
                  "Found %d links at page %s\n",
                  linksOfPage.size(),
                  page.toString()));
        } catch (IOException e) {
          // skip ones that cannot be accessed
          continue;
        }
      }

      searchLinks = nextSearchLinks;
      nextSearchLinks = new HashSet<>(searchLinks.size() * AVG_LINKS_PER_PAGE);
      curDepth++;

      // for debugging
      System.out.printf(
          String.format(
              "**** Arrived at depth %d; iterating over %d links ****\n\n",
              curDepth,
              searchLinks.size()));
    }

  }

  /**
   * Starts scraping by going down a random child of all links. Stops if depth
   * is not equal to -1
   */
  public void startRandomDescentScrape() {
    int curDepth = 0;
    Set<WikiPage> links;

    try {
      links = linkFinder.linkedPages(startPage);
    } catch (IOException e) {
      throw new AssertionError(
          "Start page not reachable: " + e.getMessage(),
          e);
    }

    List<WikiPage> accessiblePages = new ArrayList<>();
    while (depth == -1 || curDepth < depth) {
      for (WikiPage page : links) {
        // cache all edges of each
        try {
          linkFinder.linkedPages(page);
          // note accessible
          accessiblePages.add(page);
        } catch (IOException e) {
          // skip failed ones
          continue;
        }
      }
      // then choose one for the next iteration (cycling until we get one)
      assert accessiblePages.size() > 0;

      WikiPage randPage = accessiblePages
          .get((int) (Math.random() * accessiblePages.size()));

      try {
        links = linkFinder.linkedPages(randPage);
      } catch (IOException e) {
        // this should not happen
        throw new AssertionError();
      }
    }
    curDepth++;
  }
}
