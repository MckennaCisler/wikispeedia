package edu.brown.cs.jmrs.web;

import java.util.Objects;

import edu.brown.cs.jmrs.collect.graph.AbstractEdge;

/**
 * A link between Pages, for use in graph algorithms.
 *
 * @author mcisler
 *
 */
public class Link extends AbstractEdge<Page, Link> {
  private final Page page1;
  private final Page page2;

  /**
   * Constructs a link between two pages, without loss of generality.
   *
   * @param page1
   *          The first page.
   * @param page2
   *          The second page.
   */
  public Link(Page page1, Page page2) {
    this.page1 = page1;
    this.page2 = page2;
  }

  @Override
  public Integer getValue() {
    return 1; // TODO
  }

  @Override
  public boolean isDirected() {
    return false;
  }

  @Override
  public Page getFirst() {
    return page1;
  }

  @Override
  public Page getSecond() {
    return page2;
  }

  @Override
  public int hashCode() {
    return Objects.hash(page1, page2);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Link other = (Link) obj;
    return this.page1.equals(other.page1) && this.page2.equals(other.page2)
        || this.page1.equals(other.page2) && this.page2.equals(other.page1);
  }

}
