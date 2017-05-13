package edu.brown.cs.jmrs.collect.graph;

import java.util.Objects;

import edu.brown.cs.jmrs.collect.graph.Graph.Edge;
import edu.brown.cs.jmrs.collect.graph.Graph.Node;

/**
 * An abstract implementation of a Node that provides some useful method
 * implementations.
 *
 * Note that AbstactNode only determines its hashCode and equality from its
 * value, not it's edges, to avoid infinite reference loops. Other nodes should
 * define their own notion of unique hash codes and deeper equality by
 * overriding these methods.
 *
 * @author mcisler
 *
 * @param <N>
 *          The particular node implementation.
 * @param <E>
 *          The particular edge implementation.
 */
public abstract class AbstractNode<N extends Node<N, E>, E extends Edge<N, E>>
    implements Node<N, E> {

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public int hashCode() {
    return Objects.hash((Object) getValue()); // have to do this for some reason
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    if (other.getClass() != getClass()) {
      return false;
    }
    Node<N, E> otherNode = (Node<N, E>) other;

    return getValue().equals(otherNode.getValue());
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Node (" + getValue() + ")";
  }
}
