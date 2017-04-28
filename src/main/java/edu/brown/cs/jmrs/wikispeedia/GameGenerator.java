package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

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
  private static final int OFF_START_PAGE = 3;

  private GameGenerator() {
    // override default constructor
  }

  /**
   * Generates a WikiGame (essentially pair of pages) which .
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
  /* Helpers for page traversal */
  /**************************************************************/

  private static WikiPage goDownFrom(WikiPage start, int depth) {
    if (depth == 0) {
      return start;
    }
    return goDownFrom(getRandomLink(start), depth - 1);
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

  private static WikiPage getRandomPage() {
    // Start from the main page for rapidly-changing and diverse content
    return goDownFrom(START_PAGE, OFF_START_PAGE);
  }

  /**************************************************************/
  /* Helpers for difficulty judging */
  /**************************************************************/

  /**
   * Determines how "obscure" the concept in the given page is.
   *
   * @param page
   *          The page to determine the "obscurity" of.
   * @return A positive integer representing the "obscurity" of a page where
   *         larger values are more obscure concepts and smaller values
   *         represent simpler concepts.
   */
  private static int obscurity(WikiPage page) {
    return 1;
  }
}
