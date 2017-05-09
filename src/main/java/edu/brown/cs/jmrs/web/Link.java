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
  private final Page src;
  private final Page dest;

  /**
   * Constructs a link between two pages, where src links to dest.
   *
   * @param src
   *          The page linking to dest.
   * @param dest
   *          The page linked to from src.
   */
  public Link(Page src, Page dest) {
    this.src = src;
    this.dest = dest;
  }

  @Override
  public Integer getValue() {
    // no obvious value; defer to edgeFinder
    return 1;
  }

  @Override
  public boolean isDirected() {
    return true;
  }

  @Override
  public Page getFirst() {
    return src;
  }

  @Override
  public Page getSecond() {
    return dest;
  }

  @Override
  public int hashCode() {
    return Objects.hash(src, dest);
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
    return this.src.equals(other.src) && this.dest.equals(other.dest)
        || this.src.equals(other.dest) && this.dest.equals(other.src);
  }

}
