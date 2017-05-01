package edu.brown.cs.jmrs.collect.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.brown.cs.jmrs.collect.graph.Graph.Edge;
import edu.brown.cs.jmrs.collect.graph.Graph.Node;

/**
 * An interface representing a method for finding edges between nodes.
 *
 *
 * Note that the default neighbors() method calls edge(), so it should be
 * overridden if edge() is slow or there is a generally better method of getting
 * neighbors.
 *
 * @author mcisler
 *
 * @param <N>
 *          The particular node implementation.
 * @param <E>
 *          The particular edge implementation.
 */
public interface EdgeFinder<N extends Node<N, E>, E extends Edge<N, E>> {
  /**
   * Finds all edges from the given node under the parameters of this particular
   * implementation.
   *
   * @param node
   *          The node to find edges of.
   * @return A Set of edges from the node, in no particular order. If the parent
   *         graph is directed, this can contain both incoming and outgoing
   *         edges.
   */
  Set<E> edges(N node);

  /**
   * Finds the value of an edge under the parameters of this particular
   * implementation. Should be used in PathFinder implementations to allow for
   * dynamic edge finding.
   *
   * @param edge
   *          The edge to find the value of.
   * @return The (Numeric) value of this edge.
   */
  Number edgeValue(E edge);

  /**
   * A default implementation of the required neighbors() method that uses
   * edges() to find non-self neighbors (unless one neighbor is
   * self-referencing).
   *
   * @param node
   *          The node to get neighbors of, using edge(node).
   *
   * @return The set of neighbor Nodes of this node, without any direct
   *         information about edges between them.
   */
  default Set<N> neighbors(N node) {
    Collection<? extends E> edges = edges(node);
    Set<N> neighbors = new HashSet<>(edges.size());

    for (E edge : edges) {
      // only add this node to the neighbors list if it's a loop, i.e. if both
      // first and second are this node.
      boolean firstNodeThis = edge.getFirst().equals(this);
      boolean secondNodeThis = edge.getSecond().equals(this);
      if (firstNodeThis && secondNodeThis) {
        neighbors.add((N) this);
      } else if (!secondNodeThis) {
        neighbors.add(edge.getSecond());
      } else if (!firstNodeThis) {
        neighbors.add(edge.getFirst());
      } else {
        throw new AssertionError(
            "A Node had an edge that did not contain itself as an endpoint");
      }
    }
    return neighbors;
  }
}
