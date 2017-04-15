package edu.brown.cs.jmrs.web;

import java.io.IOException;
import java.util.Set;

/**
 * Finds all outgoing links on a page, the most basic link finder.
 *
 * @author mcisler
 *
 */
public class AllLinkFinder implements LinkFinder {

  @Override
  public Set<String> links(Page page) throws IOException {
    return new LinkFinderMethod<Page>().links(page);
  }
}
