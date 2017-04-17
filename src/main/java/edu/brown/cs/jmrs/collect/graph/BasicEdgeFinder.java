package edu.brown.cs.jmrs.collect.graph;

import java.util.Set;

/**
 * A basic implementation of an EdgeFinder using other basic graph
 * Implementations.
 *
 * @author mcisler
 *
 * @param <NV>
 *          The type of object occupying nodes.
 * @param <EV>
 *          The type of object defining edge values between nodes. Must be
 *          numeric for use by distance-finding algorithms.
 */
public class BasicEdgeFinder<NV, EV extends Number>
    implements EdgeFinder<BasicNode<NV, EV>, BasicEdge<NV, EV>> {

  @Override
  public Set<BasicEdge<NV, EV>> edges(BasicNode<NV, EV> node) {
    return node.getEdges();
  }
}
