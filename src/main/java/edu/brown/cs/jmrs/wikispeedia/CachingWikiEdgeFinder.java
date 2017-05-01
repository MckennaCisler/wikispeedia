package edu.brown.cs.jmrs.wikispeedia;

import java.util.Set;

import edu.brown.cs.jmrs.collect.graph.EdgeFinder;
import edu.brown.cs.jmrs.web.Link;
import edu.brown.cs.jmrs.web.Page;

/**
 * An edge finder for use in graph searching on WikiPages that uses a database
 * cache of edges to speed up operations.
 *
 * @author mcisler
 *
 */
public class CachingWikiEdgeFinder implements EdgeFinder<Page, Link> {

  @Override
  public Set<Link> edges(Page node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Number edgeValue(Link edge) {
    // TODO?
    return 1;
  }
}
