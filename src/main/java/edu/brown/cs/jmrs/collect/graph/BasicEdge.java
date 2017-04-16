package edu.brown.cs.jmrs.collect.graph;

/**
 * A simple implementation of an Edge. Note: Not tested due to it's simplicity.
 *
 * @author mcisler
 *
 * @param <NV>
 *          The type of object occupying nodes.
 * @param <EV>
 *          The type of object defining edge values between nodes. Must be
 *          numeric for use by distance-finding algorithms.
 */
public class BasicEdge<NV, EV extends Number>
    extends AbstractEdge<BasicNode<NV, EV>, BasicEdge<NV, EV>> {
  private final boolean directed;
  private final EV value;
  private final BasicNode<NV, EV> node1; // may be the source
  private final BasicNode<NV, EV> node2; // may be the destination

  /**
   * Constructs an undirected edge from the given values.
   *
   * @param value
   *          The (comparable) value of this edge.
   * @param node1
   *          The first node.
   * @param node2
   *          The second node.
   */
  public BasicEdge(EV value, BasicNode<NV, EV> node1, BasicNode<NV, EV> node2) {
    this(value, node1, node2, false);
  }

  /**
   * Constructs an edge from the given values, with a specified direction.
   *
   * @param value
   *          The (comparable) value of this edge.
   * @param node1
   *          The first node, or the source node if directed is true;
   * @param node2
   *          The second node, or the destination node if directed is false.
   * @param directed
   *          Whether this edge is directed.
   */
  public BasicEdge(EV value, BasicNode<NV, EV> node1, BasicNode<NV, EV> node2,
      boolean directed) {
    this.directed = directed;
    this.value = value;
    this.node1 = node1;
    this.node2 = node2;
  }

  @Override
  public EV getValue() {
    return value;
  }

  @Override
  public boolean isDirected() {
    return directed;
  }

  @Override
  public BasicNode<NV, EV> getFirst() {
    return node1;
  }

  @Override
  public BasicNode<NV, EV> getSecond() {
    return node2;
  }
}
