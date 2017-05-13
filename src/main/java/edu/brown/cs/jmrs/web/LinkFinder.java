package edu.brown.cs.jmrs.web;

import java.io.IOException;
import java.util.Set;

import com.google.common.util.concurrent.UncheckedExecutionException;

import edu.brown.cs.jmrs.collect.Functional;
import edu.brown.cs.jmrs.collect.graph.EdgeFinder;

/**
 * An interface for a method to find outgoing links of a Page.
 *
 * @author mcisler
 *
 * @param <P>
 *          The page implementation to take in and use to create for links.
 */
public interface LinkFinder<P extends Page> extends EdgeFinder<Page, Link> {

  /**
   * Finds the outgoing links from the given page.
   *
   * @param page
   *          The root page.
   * @return A list of urls of each outgoing link. Should be a HashSet for speed
   *         in link-filtering applications.
   * @throws IOException
   *           If the page could not be reached.
   */
  Set<String> links(P page) throws IOException;

  /**
   * Finds the outgoing pages from the given page.
   *
   * @param page
   *          The root page.
   * @return A list of pages corresponding to links. Should be a HashSet for
   *         speed in link-filtering applications.
   * @throws IOException
   *           If the page could not be reached.
   */
  Set<P> linkedPages(P page) throws IOException;

  @Override
  default Set<Link> edges(Page page) {
    try {
      return Functional.map(linkedPages((P) page), (pg) -> new Link(page, pg));
    } catch (IOException e) {
      throw new UncheckedExecutionException(e);
    }
  }

  @Override
  default Number edgeValue(Link edge) {
    // generic value; no special function here
    return 1;
  }
}
