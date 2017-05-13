package edu.brown.cs.jmrs.collect.graph;

import java.util.List;

import edu.brown.cs.jmrs.collect.graph.Graph.Edge;
import edu.brown.cs.jmrs.collect.graph.Graph.Node;

/**
 * A utility to represent the abstract concept of a graph and hold methods
 * operating on the graph. Actual data of nodes, connections, and edge values is
 * stored in implementations of Nodes and in their connections made by Edges,
 * where Nodes and Edges can both store types.
 *
 * @author mcisler
 *
 * @param <N>
 *          The particular node implementation.
 * @param <E>
 *          The particular edge implementation.
 *
 */
public class Graph<N extends Node<N, E>, E extends Edge<N, E>> {
  /**
   * The following do not define the type of graph this graph is; they merely
   * define how it can be used. Methods that are reliant on these properties can
   * use them to ensure they are being called from a graph that follows those
   * properties. In addition, the graph does internal testing to ensure these
   * properties hold.
   */
  private final boolean directed;

  /**
   * Initializes an undirected, acyclic graph.
   */
  public Graph() {
    this(false);
  }

  /**
   * Initializes a graph with the provided parameters.
   *
   * @param directed
   *          Whether the graph will have directed edges.
   */
  public Graph(boolean directed) {
    this.directed = directed;
  }

  /**
   * @return Whether this graph is directed.
   */
  public final boolean isDirected() {
    return directed;
  }

  /**
   * Finds the shortest path between the given nodes using the provided
   * algorithm.
   *
   * @param node1
   *          The first node.
   * @param node2
   *          The second node.
   * @param pathFinder
   *          The path finding algorithm that will be used to find the distance
   *          between node1 and node2.
   * @return The path (list) of edges from node1 to node2 that represents the
   *         shortest path, using the provided edge values, from node1 to node2.
   */
  public List<E> shortestPath(N node1, N node2, PathFinder<N, E> pathFinder) {
    return pathFinder.shortestPath(node1, node2);
  }

  /**
   * An interface for a class representing a vertex, or node, in a Graph. Values
   * of nodes do not necessarily need to be unique in the graph.
   *
   * Additionally, nodes must be in agreement regarding the edges between them;
   * for example if one node says it has an edge with another, both should be in
   * eachother's .getNeighbors() and .getEdges(). If the graph is directed,
   * which node is the source / destination should also be agreed upon.
   *
   * @author mcisler
   *
   * @param <N>
   *          The particular node implementation.
   * @param <E>
   *          The particular edge implementation.
   */
  public interface Node<N extends Node<N, E>, E extends Edge<N, E>> {
    /**
     * @return The value at this Node. Used to determine equality between nodes.
     * @param <NV>
     *          The type of the value at this node.
     */
    <NV> NV getValue();
  }

  /**
   * An interface for a class representing an edge bridging two nodes in a
   * Graph.
   *
   * @author mcisler
   *
   * @param <N>
   *          The particular node implementation.
   * @param <E>
   *          The particular edge implementation.
   */
  public interface Edge<N extends Node<N, E>, E extends Edge<N, E>>
      extends Comparable<Edge<N, E>> {
    /**
     * @return The value at this edge. By default, this Edge uses the output of
     *         this method to compare it to other Edges.
     * @param <EV>
     *          The numeric type representing the value of this edge.
     */
    <EV extends Number> EV getValue();

    /**
     * @return Whether this edge is directed.
     */
    boolean isDirected();

    /**
     * @return The first (arbitrary) node of this edge. If this edge is
     *         directed, this corresponds to the source, but otherwise this is
     *         not guaranteed.
     */
    N getFirst();

    /**
     * @return The second (arbitrary) node of this edge. If this edge is
     *         directed, this corresponds to the destination, but otherwise this
     *         is not guaranteed.
     */
    N getSecond();

    /**
     * @return The source node of this directed edge, assuming it is directed.
     * @throws UnsupportedOperationException
     *           If this edge is not directed.
     */
    N getSource() throws UnsupportedOperationException;

    /**
     * @return The destination node of this directed edge, assuming it is
     *         directed.
     * @throws UnsupportedOperationException
     *           If this edge is not directed.
     */
    N getDestination() throws UnsupportedOperationException;
  }

  /**
   * An interface for implementing shortest-path finding algorithms in a graph.
   *
   * Note: should use EdgeFinder.edgeValue() instead of Edge.getValue() if using
   * EdgeFinders.
   *
   * @author mcisler
   *
   * @param <N>
   *          The particular node implementation.
   * @param <E>
   *          The particular edge implementation.
   */
  public interface PathFinder<N extends Node<N, E>, E extends Edge<N, E>> {
    /**
     * Finds the shortest path between the given nodes using a particular
     * algorithm.
     *
     * @param start
     *          The node to search for a path to end from.
     * @param end
     *          The node to search for a path to from start.
     * @return A list of the nodes between start and end that represent the
     *         shortest path between them.
     */
    List<E> shortestPath(N start, N end);
  }

}
