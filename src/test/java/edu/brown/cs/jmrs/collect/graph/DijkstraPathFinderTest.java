package edu.brown.cs.jmrs.collect.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import edu.brown.cs.jmrs.collect.TestingHelpers;
import edu.brown.cs.jmrs.collect.graph.BasicEdge;
import edu.brown.cs.jmrs.collect.graph.BasicEdgeFinder;
import edu.brown.cs.jmrs.collect.graph.BasicNode;
import edu.brown.cs.jmrs.collect.graph.DirectedDijkstraPathFinder;
import edu.brown.cs.jmrs.collect.graph.EdgeFinder;
import edu.brown.cs.jmrs.collect.graph.Graph.PathFinder;

/**
 * Tests the Dijkstra implementation of a path finder.
 *
 * @author mcisler
 *
 */
public class DijkstraPathFinderTest {
  private final EdgeFinder<BasicNode<String, Integer>, BasicEdge<String, Integer>> edgeFinder =
      new BasicEdgeFinder<String, Integer>();

  /**
   * Test directedness assertion.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testAssertDirected() {
    BasicNode<String, Integer> n1 = new BasicNode<>("cat");
    BasicNode<String, Integer> n2 = new BasicNode<>("bird");
    BasicNode<String, Integer> n3 = new BasicNode<>("dog");
    BasicNode<String, Integer> n4 = new BasicNode<>("free bird");
    n1.addEdge(new BasicEdge<>(5, n1, n2, true))
        .addEdge(new BasicEdge<>(3, n1, n3, false));
    n4.addEdge(new BasicEdge<>(5, n4, n3, true));

    PathFinder<BasicNode<String, Integer>, BasicEdge<String, Integer>> pf =
        new DirectedDijkstraPathFinder<>(edgeFinder);

    pf.shortestPath(n1, n4);
  }

  /**
   * Test on an empty graph (represented by neighborless nodes).
   */
  @Test
  public void testEmptyGraph() {
    BasicNode<String, Integer> n1 = new BasicNode<>("cat");
    BasicNode<String, Integer> n2 = new BasicNode<>("dog");

    PathFinder<BasicNode<String, Integer>, BasicEdge<String, Integer>> pf =
        new DirectedDijkstraPathFinder<>(edgeFinder);

    assertEquals(0, pf.shortestPath(n1, n2).size());
    assertEquals(0, pf.shortestPath(n2, n1).size());
  }

  /**
   * Test on an unconnected graph (represented by nodes with no bridge).
   */
  @Test
  public void testUnconnectedGraph() {
    BasicNode<String, Integer> n1 = new BasicNode<>("cat");
    BasicNode<String, Integer> n2 = new BasicNode<>("dog");
    BasicNode<String, Integer> n3 = new BasicNode<>("bird");
    BasicNode<String, Integer> n4 = new BasicNode<>("free bird");
    n1.addNeighbor(n2, 1, true);
    n3.addNeighbor(n4, 3, true);

    PathFinder<BasicNode<String, Integer>, BasicEdge<String, Integer>> pf =
        new DirectedDijkstraPathFinder<>(edgeFinder);

    assertTrue(TestingHelpers.containsSameElements(
        ImmutableList.of(new BasicEdge<String, Integer>(1, n1, n2, true)),
        pf.shortestPath(n1, n2)));
    assertTrue(TestingHelpers.containsSameElements(
        ImmutableList.of(new BasicEdge<String, Integer>(3, n3, n4, true)),
        pf.shortestPath(n3, n4)));

    assertEquals(0, pf.shortestPath(n2, n1).size());
    assertEquals(0, pf.shortestPath(n4, n3).size());

    assertEquals(0, pf.shortestPath(n1, n3).size());
    assertEquals(0, pf.shortestPath(n3, n1).size());
    assertEquals(0, pf.shortestPath(n2, n4).size());
    assertEquals(0, pf.shortestPath(n4, n2).size());
    assertEquals(0, pf.shortestPath(n1, n4).size());
    assertEquals(0, pf.shortestPath(n2, n3).size());
  }

  /**
   * Test on a super simple graphs (nodes).
   */
  @Test
  public void testSimpleGraph() {
    BasicNode<String, Integer> n1 = new BasicNode<>("cat");
    BasicNode<String, Integer> n2 = new BasicNode<>("bird");
    BasicNode<String, Integer> n3 = new BasicNode<>("dog");
    BasicNode<String, Integer> n4 = new BasicNode<>("free bird");
    BasicNode<String, Integer> n5 = new BasicNode<>("fly");
    n1.addNeighbor(n2, 1, true).addNeighbor(n4, 5, true);
    n2.addNeighbor(n3, 4, true);
    n3.addNeighbor(n1, 2, true).addNeighbor(n2, 10, true);
    n4.addNeighbor(n1, 5, true).addNeighbor(n1, 100, true);
    n5.addNeighbor(n2, 5, true);

    PathFinder<BasicNode<String, Integer>, BasicEdge<String, Integer>> pf =
        new DirectedDijkstraPathFinder<>(edgeFinder);

    // edge cases
    assertTrue(TestingHelpers.containsSameElements(ImmutableList.of(),
        pf.shortestPath(n1, n5)));
    assertTrue(TestingHelpers.containsSameElements(ImmutableList.of(),
        pf.shortestPath(n4, n5)));
    // make sure they're directed edges
    assertEquals(0, pf.shortestPath(n4, n5).size());
    assertTrue(pf.shortestPath(n1, n3).size() > 1);

    assertTrue(TestingHelpers.containsSameElements(
        ImmutableList.of(new BasicEdge<String, Integer>(1, n1, n2, true)),
        pf.shortestPath(n1, n2)));
    assertTrue(TestingHelpers.containsSameElements(
        ImmutableList.of(new BasicEdge<String, Integer>(5, n4, n1, true)),
        pf.shortestPath(n4, n1))); // shorter path of two
    assertTrue(TestingHelpers.containsSameElements(
        ImmutableList.of(new BasicEdge<String, Integer>(5, n1, n4, true)),
        pf.shortestPath(n1, n4)));

    assertTrue(TestingHelpers.containsSameElements(
        ImmutableList.of(new BasicEdge<String, Integer>(2, n3, n1, true),
            new BasicEdge<String, Integer>(5, n1, n4, true)),
        pf.shortestPath(n3, n4)));
    assertTrue(TestingHelpers.containsSameElements(
        ImmutableList.of(new BasicEdge<String, Integer>(4, n2, n3, true),
            new BasicEdge<String, Integer>(2, n3, n1, true)),
        pf.shortestPath(n2, n1)));
    assertTrue(TestingHelpers.containsSameElements(
        ImmutableList.of(new BasicEdge<String, Integer>(5, n4, n1, true),
            new BasicEdge<String, Integer>(1, n1, n2, true),
            new BasicEdge<String, Integer>(4, n2, n3, true)),
        pf.shortestPath(n4, n3)));
  }
}
