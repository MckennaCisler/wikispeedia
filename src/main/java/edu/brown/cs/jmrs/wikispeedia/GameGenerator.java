package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Doubles;

import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder.Filter;

/**
 * A class to generate the start and end pages of a Wikipedia game given a
 * certain set of parameters.
 *
 * @author mcisler
 *
 */
public final class GameGenerator {
  // ignore MORE pages than the game one
  private static final LinkFinder<WikiPage> WIKI_LINK_FINDER =
      new WikiPageLinkFinder(WikiLobby.DEFAULT_CONTENT_FORMATTER,
          Filter.DISAMBIGUATION, Filter.NON_ENGLISH_WIKIPEDIA, Filter.DATES,
          Filter.INTEGERS);

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
   * The distance (inclusive) between two obscurity values to be considered
   * "equivalent". Decreasing it may make generation slower and more
   * memory-intensive.
   */
  private static final double OBSCURITY_EQUAL_RANGE = 0.25;

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
  private static final double MAX_NUM_OF_PAGE_LINKS = 1000;

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
    return new WikiGame(start, goal, ImmutableSet.of());
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
    return ofDist(pageDist, false);
  }

  /**
   * Generates a WikiGame (essentially pair of pages) the given distance apart.
   *
   * @param pageDist
   *          The distance the pages should be apart.
   * @param withSpace
   *          Whether to include the space of pages found during generation.
   * @return The WikiGame defined by both pages.
   */
  public static WikiGame ofDist(int pageDist, boolean withSpace) {
    WikiPage start = getRandomPage();
    if (withSpace) {
      Set<WikiPage> space = new HashSet<>();
      return new WikiGame(start, goDownFrom(start, pageDist, space), space);
    } else {
      return new WikiGame(start, goDownFrom(start, pageDist),
          ImmutableSet.of());
    }
  }

  /**************************************************************/
  /* Helpers for page traversal / basic generation */
  /**************************************************************/

  /**
   * Goes down from start to a page at depth depth.
   *
   * @param start
   *          The start page.
   * @param depth
   *          The depth to go down.
   * @return The destination page.
   */
  public static WikiPage goDownFrom(WikiPage start, int depth) {
    if (depth == 0) {
      return start;
    }

    return goDownFrom(getRandomLink(start), depth - 1);
  }

  /**
   * Goes down from start to a page at depth depth.
   *
   * @param start
   *          The start page.
   * @param depth
   *          The depth to go down.
   * @param space
   *          The space list to populate on the way down.
   * @return The destination page.
   */
  public static WikiPage goDownFrom(WikiPage start, int depth,
      Set<WikiPage> space) {
    if (depth == 0) {
      return start;
    }

    return goDownFrom(getRandomLink(start, space), depth - 1, space);
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

    if (pages.size() == 0) {
      // defer upwards
      throw new IOException("No valid pages found when cycling");
    }

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
  static WikiPage getRandomLink(WikiPage page) {
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

  /**
   * Gets a random link (WikiPage) outgoing from a page, adding to the given
   * space.
   *
   * @param The
   *          space to add to.
   *
   * @return A WikiPage off page that is guaranteed to be accessible.
   */
  static WikiPage getRandomLink(WikiPage page, Set<WikiPage> space) {
    try {
      Set<WikiPage> links = WIKI_LINK_FINDER.linkedPages(page);
      space.addAll(links);

      int tries = 0;
      while (tries < links.size()) {
        WikiPage newPage = getRandomLinkFromPages(links);
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

  private static WikiPage getRandomLinkFromPages(Set<WikiPage> links) {
    return new ArrayList<>(links).get((int) (Math.random() * links.size()));
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
