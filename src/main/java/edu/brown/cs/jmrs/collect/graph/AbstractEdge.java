package edu.brown.cs.jmrs.collect.graph;

import java.util.Objects;

import edu.brown.cs.jmrs.collect.graph.Graph.Edge;
import edu.brown.cs.jmrs.collect.graph.Graph.Node;

/**
 * An Abstract version of an edge implementing several features based on some
 * reasonable assumptions about its subclass.
 *
 * @author mcisler
 *
 * @param <N>
 *          The particular node implementation.
 * @param <E>
 *          The particular edge implementation.
 */
public abstract class AbstractEdge<N extends Node<N, E>, E extends Edge<N, E>>
    implements Edge<N, E> {
  @Override
  public N getSource() throws UnsupportedOperationException {
    if (!isDirected()) {
      throw new UnsupportedOperationException(
          "Source node is not defined for an undirected node");
    }
    return getFirst();
  }

  @Override
  public N getDestination() throws UnsupportedOperationException {
    if (!isDirected()) {
      throw new UnsupportedOperationException(
          "Destination node is not defined for an undirected node");
    }
    return getSecond();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return Objects.hash(isDirected(), getFirst(), getSecond(), getValue());
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    Edge<N, E> other = (Edge<N, E>) obj;

    return isDirected() == other.isDirected()
        && getValue().equals(other.getValue())
        && getFirst().equals(other.getFirst())
        && getSecond().equals(other.getSecond());
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // NOTE: Don't call node print methods to avoid infinite loops
    return getFirst().getValue() + (isDirected() ? " -> " : " -- ")
        + getSecond().getValue() + " : " + getValue();
  }

  @Override
  public int compareTo(Edge<N, E> o) {
    // Uses doubleValue to compare because it provides the most precision and
    // range of values (there may still be issues)
    return Double.compare(getValue().doubleValue(), o.getValue().doubleValue());
  }
}
