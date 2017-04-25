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

  private GameGenerator() {
    // override default constructor
  }

  /**
   * Generates a WikiGame (essentially pair of pages) the given distance apart.
   *
   * @param pageDist
   *          The distance the pages should be apart.
   * @return The WikiGame.
   */
  public static WikiGame ofDist(int pageDist) {
    WikiPage start = WikiPage.fromName("Cat"); // TODO:

    WikiPage cur = start;
    for (int i = 0; i < pageDist; i++) {
      try {
        Set<String> links = WIKI_LINK_FINDER.links(cur);

        cur =
            new WikiPage(new ArrayList<>(links)
                .get((int) (Math.random() * links.size())));
      } catch (IOException e) {
        return new WikiGame(start, cur); // just give up, for now TODO:
      }
    }

    return new WikiGame(start, cur);
  }
}
