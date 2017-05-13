package edu.brown.cs.jmrs.collect.graph;

import java.util.HashSet;
import java.util.Set;

/**
 * A class with the minimum required functionality of a Graph Node.
 *
 * @author mcisler
 *
 * @param <NV>
 *          The type of object occupying nodes.
 * @param <EV>
 *          The type of object defining edge values between nodes. Must be
 *          numeric for use by distance-finding algorithms.
 */
public class BasicNode<NV, EV extends Number>
    extends AbstractNode<BasicNode<NV, EV>, BasicEdge<NV, EV>> {
  private final NV value;
  private final Set<BasicEdge<NV, EV>> edges;

  /**
   * Constructs a basic node with the provided value. Add edges with addEdge().
   *
   * @param value
   *          The value at this node.
   */
  public BasicNode(NV value) {
    this.edges = new HashSet<>();
    this.value = value;
  }

  /**
   * Adds the given edge to this node. Should only be used in construction of a
   * node/graph.
   *
   * @param edge
   *          The edge to add
   * @return This object, for further additions if necessary.
   */
  public BasicNode<NV, EV> addEdge(BasicEdge<NV, EV> edge) {
    // equal in reference specifically
    assert edge.getFirst() == this || edge.getSecond() == this;
    edges.add(edge);
    return this;
  }

  /**
   * Adds the given node as a neighbor to this node with the given edge value.
   * Should only be used in construction of a node/graph.
   *
   * @param node
   *          The node to add an edge too.
   * @param val
   *          The value the edge to node from this node should have.
   * @return This object, for further additions if necessary.
   */
  public BasicNode<NV, EV> addNeighbor(BasicNode<NV, EV> node, EV val) {
    return addNeighbor(node, val, false);
  }

  /**
   * Adds the given node as a neighbor to this node with the given edge value.
   * Should only be used in construction of a node/graph.
   *
   * @param node
   *          The node to add an edge too.
   * @param val
   *          The value the edge to node from this node should have.
   * @param directed
   *          Whether the new edge should be directed.
   * @return This object, for further additions if necessary.
   */
  public BasicNode<NV, EV> addNeighbor(BasicNode<NV, EV> node, EV val,
      boolean directed) {
    return addEdge(new BasicEdge<NV, EV>(val, this, node, directed));
  }

  /**
   * Adds the given node as a source of an edge from it to this neighbor, with
   * the given edge value. Should only be used in construction of a node/graph.
   *
   * @param source
   *          The desired source node of the new edge pointing to this node.
   * @param val
   *          The value the edge between source and this node should have.
   * @return This object, for further additions if necessary.
   */
  public BasicNode<NV, EV> addNeighborAsSource(BasicNode<NV, EV> source,
      EV val) {
    return addEdge(new BasicEdge<NV, EV>(val, source, this, true));
  }

  /**
   * Adds the given node as a destination of an edge to it from this neighbor,
   * with the given edge value. Should only be used in construction of a
   * node/graph.
   *
   * @param dest
   *          The desired destination node of the new edge pointing away from
   *          this node.
   * @param val
   *          The value the edge between dest and this node should have.
   * @return This object, for further additions if necessary.
   */
  public BasicNode<NV, EV> addNeighborAsDest(BasicNode<NV, EV> dest, EV val) {
    return addEdge(new BasicEdge<NV, EV>(val, this, dest, true));
  }

  @Override
  public NV getValue() {
    return value;
  }

  /**
   * A utility function for getting internal edges.
   *
   * @return The edges of this node as BasicEdges.
   */
  public Set<BasicEdge<NV, EV>> getEdges() {
    return edges;
  }
}
