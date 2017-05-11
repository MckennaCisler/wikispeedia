package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.primitives.Doubles;

import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * A class to generate the start and end pages of a Wikipedia game given a
 * certain set of parameters.
 *
 * @author mcisler
 *
 */
public final class GameGenerator {
  private static final LinkFinder<WikiPage> WIKI_LINK_FINDER =
      WikiLobby.DEFAULT_LINK_FINDER;

  private static final WikiPage START_PAGE = WikiPage.fromName("Main_Page");

  static {
    // check and cache if found
    assert START_PAGE.accessible(true);
  }

  /**
   * Page generation constants
   */
  /**
   * how far to go off START_PAGE before choosing a page at random.
   */
  private static final int OFF_START_PAGE = 3;

  /**
   * a value that determines how quickly the predicate-based page finder moves
   * on from a single page's links to search deeper.
   *
   * Smaller values lead to greater depth searches with less consideration of
   * each page's links. It is essentially the chance that a given link on a page
   * will be skipped, so 0.99 will approach checking a single link on a page and
   * 0 will check all links.
   *
   * NOTE: Do not set to exactly 1, because the generation will never stop.
   *
   * Decreasing it may make generation slower and more memory-intensive.
   */
  private static final double DEPTH_BREADTH_SEARCH_RATIO = 0.5;

  /**
   * the distance (inclusive) between two obscurity value to be considered
   * "equivalent". Increasing it may make generation slower and more
   * memory-intensive.
   */
  private static final double OBSCURITY_EQUAL_RANGE = 0.1;

  /**
   * The expected largest number of Wikipedia links on a page (under the
   * linkFinder that is used; at least the most average one). A good highest
   * number is from /wiki/List_of_2016_albums (3080); other candidates are best
   * found here: https://en.wikipedia.org/wiki/Special:LongPages
   *
   * You may actually want to set this a bit lower than the very largest; as
   * pages with such numbers of links are often abnormal pages such as lists,
   * where the link number has little to do with obscurity. Also these pages are
   * very rare and thus slow to find.
   */
  private static final double MAX_NUM_OF_PAGE_LINKS = 2000;

  private GameGenerator() {
    // override default constructor
  }

  /**
   * Generates a WikiGame (essentially pair of pages) that have an approximate
   * obscurity value.
   *
   * @param obscurity
   *          The target obscurity of the two pages.
   * @return The WikiGame defined by both pages.
   */
  public static WikiGame withObscurity(double obscurity) {
    WikiPage start;
    WikiPage goal;

    do {
      start = pageWithObscurity(obscurity);
      goal = pageWithObscurity(obscurity);

    } while (start.equalsAfterRedirectSafe(goal));
    return new WikiGame(start, goal);
  }

  /**
   * Generates a WikiPage that has an approximate obscurity value.
   *
   * @param obscurity
   *          The target obscurity of the page.
   * @return A WikiPage with that approximate obscurity.
   */
  public static WikiPage pageWithObscurity(double obscurity) {
    assert obscurity <= 1 && obscurity >= 0;
    return getRandomPage(obscurityFilter(obscurity));
  }

  private static Predicate<WikiPage> obscurityFilter(double obscurity) {
    return (page) -> {
      return Math.abs(obscurity - obscurity(page)) <= OBSCURITY_EQUAL_RANGE;
    };
  }

  /**
   * Generates a WikiGame (essentially pair of pages) the given distance apart.
   *
   * @param pageDist
   *          The distance the pages should be apart.
   * @return The WikiGame defined by both pages.
   */
  public static WikiGame ofDist(int pageDist) {
    WikiPage start = getRandomPage();
    return new WikiGame(start, goDownFrom(start, pageDist));
  }

  /**************************************************************/
  /* Helpers for page traversal / basic generation */
  /**************************************************************/

  private static WikiPage goDownFrom(WikiPage start, int depth) {
    if (depth == 0) {
      return start;
    }

    return goDownFrom(getRandomLink(start), depth - 1);
  }

  /**
   * @param start
   *          The page to start from
   * @param stop
   *          The predicate to stop searching for upon returning true. Probably
   *          shouldn't request page HTML.
   * @return A page satisfying the predicate, somewhere after start.
   * @throws IOException
   *           If a page could not be accessed; this defers the decision of what
   *           to do upwards, because if we attempt to try more links in this
   *           function we could easily get into infinite loops.
   */
  private static WikiPage goDownFrom(WikiPage start, Predicate<WikiPage> stop)
      throws IOException {
    // don't check start because it will be later
    Set<WikiPage> pages = WIKI_LINK_FINDER.linkedPages(start);

    for (WikiPage page : pages) {
      if (Math.random() > DEPTH_BREADTH_SEARCH_RATIO && stop.test(page)) {
        return page;
      }
    }
    return goDownFrom(
        new ArrayList<>(pages).get((int) (Math.random() * pages.size())), stop);
  }

  /**
   * Same as goDownFrom, but starts from a random link of start and retries
   * other links on failure of that path.
   */
  private static WikiPage goDownFromRetrying(WikiPage start,
      Predicate<WikiPage> stop) {
    try {
      Set<String> links = WIKI_LINK_FINDER.links(start);
      int tries = 0;
      while (tries < links.size()) {
        try {
          // try to go down from this link; try again down a different path on
          // failure.
          return goDownFrom(getRandomLinkFrom(links), stop);
        } catch (IOException e) {
          tries++;
        }
      }
      // something is wrong, no links from start page accessible
      throw new AssertionError(
          "Links from start page '" + START_PAGE + "' cannot be accessed");
    } catch (IOException e) {
      throw new AssertionError(
          "Start page '" + START_PAGE + "' cannot be accessed");
    }
  }

  /**
   * Gets a random link (WikiPage) outgoing from a page.
   *
   * @return A WikiPage off page that is guaranteed to be accessible.
   */
  private static WikiPage getRandomLink(WikiPage page) {
    try {
      Set<String> links = WIKI_LINK_FINDER.links(page);
      int tries = 0;
      while (tries < links.size()) {
        WikiPage newPage = getRandomLinkFrom(links);
        if (newPage.accessible(true)) {
          return newPage;
        } else {
          Main.debugLog("Could not access page: " + newPage);
        }
        tries++;
      }
      // if we run out of tries
    } catch (IOException e) {
      // if root page error occurs
    }
    return page;
  }

  private static WikiPage getRandomLinkFrom(Set<String> links) {
    return new WikiPage(
        new ArrayList<>(links).get((int) (Math.random() * links.size())),
        Main.WIKI_PAGE_DOC_CACHE);
  }

  /**
   * @return A pseudo-random page with little parsing. Underlying implementation
   *         simply moves a distance off a very general Wikipedia page.
   */
  public static WikiPage getRandomPage() {
    // Start from the main page for rapidly-changing and diverse content
    return goDownFrom(START_PAGE, OFF_START_PAGE);
  }

  /**
   * @return A pseudo-random page with little parsing. Underlying implementation
   *         simply moves a distance off a very general Wikipedia page.
   * @param stop
   *          The predicate to stop searching for upon returning true. Probably
   *          shouldn't request page HTML.
   */
  public static WikiPage getRandomPage(Predicate<WikiPage> stop) {
    // Start from the main page for rapidly-changing and diverse content, but
    // start a level down (at random page) because predicate-based one can be
    // very predictable under some parameters.
    return goDownFromRetrying(START_PAGE, stop);
  }

  /**************************************************************/
  /* Helpers for difficulty judging */
  /**************************************************************/

  /**
   * Determines how "obscure" the concept in the given page is.
   *
   * @param page
   *          The page to determine the "obscurity" of.
   * @return A positive integer from 0 to 1 representing the "obscurity" of a
   *         page where larger values are more obscure concepts and smaller
   *         values represent simpler concepts.
   */
  private static double obscurity(WikiPage page) {
    try {
      double linkRatio =
          WIKI_LINK_FINDER.links(page).size() / MAX_NUM_OF_PAGE_LINKS;

      // make sure if we find a page with more links that we don't go negative
      // (note we can never go over one - page links can't be negative). This
      // also server to cluster together the small number of high-link pages, to
      // speed things up.
      return Doubles.compare(linkRatio, 1.0) > 0 ? 0 : 1 - linkRatio;
    } catch (IOException e) {
      return 5; // pages that can't be reached are "very" obscure
    }
  }
}
