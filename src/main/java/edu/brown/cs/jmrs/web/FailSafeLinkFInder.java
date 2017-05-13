/**
 * 
 */
package edu.brown.cs.jmrs.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A decorator pattern for link finders that gracefully fails on IOExceptions.
 *
 * @author mcisler
 * @param <P>
 *          The page type of this and the internal link finder.
 */
public class FailSafeLinkFInder<P extends Page> implements LinkFinder<P> {

  private final LinkFinder<P> linkFinder;

  /**
   * @param linkFinder
   *          The linkFinder to wrap and protect from IOExceptions.
   */
  public FailSafeLinkFInder(LinkFinder<P> linkFinder) {
    this.linkFinder = linkFinder;
  }

  @Override
  public Set<String> links(P page) {
    try {
      return linkFinder.links(page);
    } catch (IOException e) {
      return new HashSet<>(0);
    }
  }

  @Override
  public Set<P> linkedPages(P page) throws IOException {
    try {
      return linkFinder.linkedPages(page);
    } catch (IOException e) {
      return new HashSet<>(0);
    }
  }

}
