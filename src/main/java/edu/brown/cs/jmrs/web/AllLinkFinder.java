package edu.brown.cs.jmrs.web;

import java.io.IOException;
import java.util.Set;
import java.util.function.Function;

/**
 * Finds all outgoing links on a page, the most basic link finder.
 *
 * @author mcisler
 * @param <P>
 *          The page type to find all the links of.
 */
public class AllLinkFinder<P extends Page> implements LinkFinder<P> {
  private final LinkFinderMethod<P> linkFinderMethod;

  /**
   * Constructs an AllLinkFinder.
   *
   * @param factory
   *          The function to be used to create neighbor pages.
   */
  public AllLinkFinder(Function<String, P> factory) {
    this.linkFinderMethod = new LinkFinderMethod<P>().factory(factory);
  }

  @Override
  public Set<String> links(Page page) throws IOException {
    return linkFinderMethod.links(page);
  }

  @Override
  public Set<P> linkedPages(P page) throws IOException {
    return linkFinderMethod.linkedPages(page);
  }
}
