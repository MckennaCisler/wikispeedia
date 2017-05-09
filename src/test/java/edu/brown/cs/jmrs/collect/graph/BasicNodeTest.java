package edu.brown.cs.jmrs.collect.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import edu.brown.cs.jmrs.collect.TestingHelpers;

/**
 * A class to test the functions of a BasicNode, and indirectly an BasicNode's
 * methods.
 *
 * @author mcisler
 *
 */
public class BasicNodeTest {
  private final EdgeFinder<BasicNode<String, Integer>, BasicEdge<String, Integer>> edgeFinder =
      new BasicEdgeFinder<String, Integer>();

  /**
   * Test method for
   * {@link edu.brown.cs.jmrs.collect.graph.BasicNode#getNeighbors()}.
   */
  @Test
  public void testGetNeighbors() {
    BasicNode<String, Integer> n1 = new BasicNode<>("cat");
    BasicNode<String, Integer> n2 = new BasicNode<>("bird");
    BasicNode<String, Integer> n3 = new BasicNode<>("dog");
    BasicNode<String, Integer> n4 = new BasicNode<>("free bird");
    assertEquals(0, edgeFinder.neighbors(n1).size());
    assertEquals(0, edgeFinder.neighbors(n2).size());
    assertEquals(0, edgeFinder.neighbors(n3).size());
    assertEquals(0, edgeFinder.neighbors(n4).size());

    n1.addNeighbor(n2, 1).addNeighbor(n3, 2).addNeighbor(n4, 5);
    n2.addNeighbor(n1, 1).addNeighbor(n3, 4);
    n3.addNeighbor(n1, 2).addNeighbor(n2, 4);
    n4.addNeighbor(n1, 5);

    assertTrue(TestingHelpers.containsSameElements(edgeFinder.neighbors(n1),
        ImmutableList.of(n2, n3, n4)));

    assertTrue(TestingHelpers.containsSameElements(edgeFinder.neighbors(n2),
        ImmutableList.of(n1, n3)));

    assertTrue(TestingHelpers.containsSameElements(edgeFinder.neighbors(n3),
        ImmutableList.of(n1, n2)));

    assertTrue(TestingHelpers.containsSameElements(edgeFinder.neighbors(n4),
        ImmutableList.of(n1)));
  }

  /**
   * Test method for
   * {@link edu.brown.cs.jmrs.collect.graph.BasicNode#getEdges()}.
   */
  @Test
  public void testGetEdges() {
    BasicNode<String, Integer> n1 = new BasicNode<>("cat");
    BasicNode<String, Integer> n2 = new BasicNode<>("bird");
    BasicNode<String, Integer> n3 = new BasicNode<>("dog");
    BasicNode<String, Integer> n4 = new BasicNode<>("free bird");
    assertEquals(0, edgeFinder.neighbors(n1).size());
    assertEquals(0, edgeFinder.neighbors(n2).size());
    assertEquals(0, edgeFinder.neighbors(n3).size());
    assertEquals(0, edgeFinder.neighbors(n4).size());

    BasicEdge<String, Integer> e12 = new BasicEdge<String, Integer>(1, n1, n2);
    BasicEdge<String, Integer> e13 = new BasicEdge<String, Integer>(2, n1, n3);
    BasicEdge<String, Integer> e14 = new BasicEdge<String, Integer>(4, n1, n4);
    BasicEdge<String, Integer> e23 = new BasicEdge<String, Integer>(5, n2, n3);

    n1.addEdge(e12).addEdge(e13).addEdge(e14);
    n2.addEdge(e12).addEdge(e23);
    n3.addEdge(e13).addEdge(e23);
    n4.addEdge(e14);

    assertTrue(TestingHelpers.containsSameElements(n1.getEdges(),
        ImmutableList.of(e12, e13, e14)));

    assertTrue(TestingHelpers.containsSameElements(n2.getEdges(),
        ImmutableList.of(e12, e23)));

    assertTrue(TestingHelpers.containsSameElements(n3.getEdges(),
        ImmutableList.of(e13, e23)));

    assertTrue(TestingHelpers.containsSameElements(n4.getEdges(),
        ImmutableList.of(e14)));
  }

  /**
   * Test method for
   * {@link edu.brown.cs.jmrs.collect.graph.BasicNode#getEdges()}.
   */
  @Test
  public void testGetEdgesDirected() {
    BasicNode<String, Integer> n1 = new BasicNode<>("cat");
    BasicNode<String, Integer> n2 = new BasicNode<>("bird");
    BasicNode<String, Integer> n3 = new BasicNode<>("dog");
    BasicNode<String, Integer> n4 = new BasicNode<>("free bird");
    assertEquals(0, edgeFinder.neighbors(n1).size());
    assertEquals(0, edgeFinder.neighbors(n2).size());
    assertEquals(0, edgeFinder.neighbors(n3).size());
    assertEquals(0, edgeFinder.neighbors(n4).size());

    BasicEdge<String, Integer> e12 =
        new BasicEdge<String, Integer>(1, n1, n2, true);
    BasicEdge<String, Integer> e13 =
        new BasicEdge<String, Integer>(2, n1, n3, true);
    BasicEdge<String, Integer> e14 =
        new BasicEdge<String, Integer>(4, n1, n4, true);
    BasicEdge<String, Integer> e23 =
        new BasicEdge<String, Integer>(5, n2, n3, true);

    n1.addEdge(e12).addEdge(e13).addEdge(e14);
    n2.addEdge(e12).addEdge(e23);
    n3.addEdge(e13).addEdge(e23);
    n4.addEdge(e14);

    assertTrue(TestingHelpers.containsSameElements(n1.getEdges(),
        ImmutableList.of(e12, e13, e14)));

    assertTrue(TestingHelpers.containsSameElements(n2.getEdges(),
        ImmutableList.of(e12, e23)));

    assertTrue(TestingHelpers.containsSameElements(n3.getEdges(),
        ImmutableList.of(e13, e23)));

    assertTrue(TestingHelpers.containsSameElements(n4.getEdges(),
        ImmutableList.of(e14)));
  }

  /**
   * Test method for {@link edu.brown.cs.jmrs.collect.graph.BasicNode#equals()}.
   */
  @Test
  public void testEqualsObject() {
    BasicNode<String, Integer> n1 = new BasicNode<>("cat");
    BasicNode<String, Integer> n2 = new BasicNode<>("cat");
    BasicNode<String, Integer> n3 = new BasicNode<>("dog");

    assertEquals(n1, n2);
    assertNotEquals(n1, n3);
    assertNotEquals(n2, n3);

    n1.addNeighbor(n3, 1);

    assertEquals(n1, n2);
    assertFalse(
        TestingHelpers.containsSameElements(n1.getEdges(), n2.getEdges()));
    assertNotEquals(n1, n3);

    n2.addNeighbor(n3, 1);

    assertEquals(n1, n2);
    assertTrue(
        TestingHelpers.containsSameElements(n1.getEdges(), n2.getEdges()));
    assertNotEquals(n1, n3);
    assertNotEquals(n2, n3);
  }
}
