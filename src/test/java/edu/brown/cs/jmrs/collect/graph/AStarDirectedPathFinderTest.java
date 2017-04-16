package edu.brown.cs.jmrs.collect.graph;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.brown.cs.jmrs.collect.graph.AStarDirectedPathFinder;
import edu.brown.cs.jmrs.collect.graph.BasicEdge;
import edu.brown.cs.jmrs.collect.graph.BasicEdgeFinder;
import edu.brown.cs.jmrs.collect.graph.BasicNode;
import edu.brown.cs.jmrs.collect.graph.DirectedDijkstraPathFinder;
import edu.brown.cs.jmrs.collect.graph.EdgeFinder;
import edu.brown.cs.jmrs.collect.graph.Graph.PathFinder;

/**
 * @author mcisler
 *
 */
public class AStarDirectedPathFinderTest {
  private final EdgeFinder<BasicNode<Localizable, Double>, BasicEdge<Localizable, Double>> edgeFinder =
      new BasicEdgeFinder<Localizable, Double>();
  private final PathFinder<BasicNode<Localizable, Double>, BasicEdge<Localizable, Double>> pf =
      new AStarDirectedPathFinder<>(edgeFinder,
          (n1, n2) -> n1.getValue().distance(n2.getValue()));
  private final PathFinder<BasicNode<Localizable, Double>, BasicEdge<Localizable, Double>> oracle =
      new DirectedDijkstraPathFinder<>(edgeFinder);
  private static final int GRAPH_SIZE = 40; // careful, it's (n^2) n log(n)

  /**
   * Test shortest path using oracle on fully connected graph.
   */
  @Test
  public void testShortestFullyConnected() {
    testNodes(generateGraph(GRAPH_SIZE, 1));
  }

  /**
   * Test shortest path using oracle on partially connected graph.
   */
  @Test
  public void testShortestPartiallyConnected() {
    testNodes(generateGraph(GRAPH_SIZE, 0.5));
  }

  /**
   * Test shortest path using oracle on sparsely connected graph.
   */
  @Test
  public void testShortestSparselyConnected() {
    testNodes(generateGraph(GRAPH_SIZE, 0.1));
  }

  /**
   * Test shortest path using oracle on unconnected graph.
   */
  @Test
  public void testShortestUnconnected() {
    testNodes(generateGraph(GRAPH_SIZE, 0));
  }

  private void testNodes(List<BasicNode<Localizable, Double>> nodes) {
    for (BasicNode<Localizable, Double> start : nodes) {
      for (BasicNode<Localizable, Double> end : nodes) {
        if (start != end) {
          assertEquals(oracle.shortestPath(start, end),
              pf.shortestPath(start, end));
        }
      }
    }
  }

  private static final double AREA_SIZE = 10;

  /**
   * Gives list of generated nodes with random connections.
   */
  private List<BasicNode<Localizable, Double>> generateGraph(int size,
      double connectedness) {

    List<BasicNode<Localizable, Double>> nodes = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      nodes.add(new BasicNode<Localizable, Double>(
          new KDPoint(AREA_SIZE * 2 * (Math.random() - 0.5),
              AREA_SIZE * 2 * (Math.random() - 0.5))));
    }

    for (BasicNode<Localizable, Double> node : nodes) {
      for (BasicNode<Localizable, Double> neighbor : nodes) {
        if (Math.random() < connectedness) {
          node.addNeighbor(neighbor,
              node.getValue().distance(neighbor.getValue()), true);
        }
      }
    }

    return nodes;
  }
}
