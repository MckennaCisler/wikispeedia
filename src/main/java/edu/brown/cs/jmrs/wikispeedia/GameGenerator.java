package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.google.common.base.Predicate;

import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder;

/**
 * A class to generate the start and end pages of a Wikipedia game given a
 * certain set of parameters.
 *
 * @author mcisler
 *
 */
public final class GameGenerator {
  private static final LinkFinder<WikiPage> WIKI_LINK_FINDER =
      new WikiPageLinkFinder();
  private static final WikiPage START_PAGE = WikiPage.fromName("Main_Page");

  /**
   * Page generation constants
   */
  /**
   * how far to go off START_PAGE before choosing a page at random.
   */
  private static final int OFF_START_PAGE = 3;

  /**
   * a value that determines how quickly the predicate-based page finder moves
   * on from a single page's links to search deeper. Smaller values lead to
   * greater depth searches with less consideration of each page's links. It is
   * essentially the chance that a given link on a page will be skipped,so 1
   * will just continue check a single link and 0 will check all links.
   *
   * Decreasing it may make generation slower and more memory-intensive.
   */
  private static final double DEPTH_BREADTH_SEARCH_RATIO = 0.5;

  /**
   * the distance (inclusive) between two obscurity value to be considered
   * "equivalent". Increasing it may make generation slower and more
   * memory-intensive.
   */
  private static final double OBSCURITY_EQUAL_RANGE = 0.05;

  /**
   * The expected largest number of Wikipedia links on a page (under the
   * linkFinder that is used; at least the most average one). This number comes
   * from /wiki/List_of_2016_albums; other candidates are best found here:
   * https://en.wikipedia.org/wiki/Special:LongPages
   */
  private static final double MAX_NUM_OF_PAGE_LINKS = 3100;

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
    assert obscurity <= 1 && obscurity >= 0;
    WikiPage start = getRandomPage(obscurityFilter(obscurity));
    return new WikiGame(start, goDownFrom(start, obscurityFilter(obscurity)));
  }

  private static Predicate<WikiPage> obscurityFilter(double obscurity) {
    return (page) -> {
      return Math.abs(obscurity - obscurity(page)) <= OBSCURITY_EQUAL_RANGE;
    };
  }

  /**
   * Generates a WikiGame (essentially pair of pages) a .
   *
   * @param pageDist
   *          The distance the pages should be apart.
   * @return The WikiGame defined by both pages.
   */
  public static WikiGame ofDifficulty(int pageDist) {
    WikiPage start = getRandomPage();
    return new WikiGame(start, goDownFrom(start, pageDist));
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
   */
  private static WikiPage goDownFrom(WikiPage start, Predicate<WikiPage> stop) {
    // don't check start because it will be
    try {
      Set<WikiPage> pages = WIKI_LINK_FINDER.linkedPages(start);

      for (WikiPage page : pages) {
        if (Math.random() > DEPTH_BREADTH_SEARCH_RATIO && stop.test(page)) {
          return page;
        }
      }
      return goDownFrom(
          new ArrayList<>(pages).get((int) (Math.random() * pages.size())),
          stop);
    } catch (IOException e) {
      return start;
    }
  }

  private static WikiPage getRandomLink(WikiPage page) {
    try {
      Set<String> links = WIKI_LINK_FINDER.links(page);

      return new WikiPage(
          new ArrayList<>(links).get((int) (Math.random() * links.size())));
    } catch (IOException e) {
      return page;
    }
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
    // Start from the main page for rapidly-changing and diverse content
    return goDownFrom(START_PAGE, stop);
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
      // TODO
      return 1 - WIKI_LINK_FINDER.links(page).size() / MAX_NUM_OF_PAGE_LINKS;
    } catch (IOException e) {
      return 5; // pages that can't be reach are "very" obscure
    }
  }
}
