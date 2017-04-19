package edu.brown.cs.jmrs.web;

import java.io.IOException;
import java.util.Set;

/**
 * An interface for a method to find outgoing links of a Page.
 *
 * @author mcisler
 *
 * @param <P>
 *          The page implementation to take in and use to create for links.
 */
public interface LinkFinder<P extends Page> {
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

}
