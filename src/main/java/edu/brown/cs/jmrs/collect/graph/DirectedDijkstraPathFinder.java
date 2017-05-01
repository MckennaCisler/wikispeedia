package edu.brown.cs.jmrs.collect.graph;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.collect.ImmutableList;

import edu.brown.cs.jmrs.collect.graph.Graph.Edge;
import edu.brown.cs.jmrs.collect.graph.Graph.Node;
import edu.brown.cs.jmrs.collect.graph.Graph.PathFinder;

/**
 * A PathFinder implementation using Dijkstra's algorithm.
 *
 * @author mcisler
 *
 * @param <N>
 *          The particular node implementation.
 * @param <E>
 *          The particular edge implementation.
 */
public class DirectedDijkstraPathFinder<N extends Node<N, E>, E extends Edge<N, E>>
    implements PathFinder<N, E> {
  private final EdgeFinder<N, E> edgeFinder;

  /**
   * Constructs a {@link DirectedDijkstraPathFinder} using the given method to
   * find edges.
   *
   * @param edgeFinder
   *          The edge finding method to using in finding shortest paths.
   */
  public DirectedDijkstraPathFinder(EdgeFinder<N, E> edgeFinder) {
    this.edgeFinder = edgeFinder;
  }

  /**
   * @see Graph.PathFinder#shortestPath(Node, Node) Note that this
   *      implementation returns a list implemented as a LinkedList.
   */
  @Override
  public List<E> shortestPath(N start, N end) {
    assert !start.equals(end);

    Map<N, Double> shortest = new HashMap<>();
    // make sure nodes are sorted by the lowest distance FROM START, NOT the
    // lowest edge value
    PriorityQueue<E> nonFounds =
        new PriorityQueue<>(new ShortestComparator(shortest));

    Map<N, E> prevs = new HashMap<>();
    N curStart = start;
    shortest.put(curStart, 0.0);

    // this should always return because nonFounds will always eventually be
    // emptied, because it is polled every time but only added if a non-found or
    // closer node is found.
    while (true) {
      // add all the neighbors of start to nonFounds / shortest
      Collection<? extends E> startEdges = edgeFinder.edges(curStart);
      for (E edge : startEdges) {
        // this will serve as an assertion that the edges given are directed
        N destNode = edge.getDestination();

        // if we have some path to this node, only update its distance and
        // shortest path to if necessary (if it doesn't exist or is closer to
        // the root) (use doubles because it will offer the most precision and
        // range of values)
        double edgeDist =
            edgeFinder.edgeValue(edge).doubleValue() + shortest.get(curStart);
        if (!shortest.containsKey(destNode)) {
          shortest.put(destNode, edgeDist);
          prevs.put(destNode, edge);
          nonFounds.add(edge);
        } else if (Double.compare(edgeDist, shortest.get(destNode)) < 0) {
          shortest.put(destNode, edgeDist);
          prevs.put(destNode, edge);
          nonFounds.removeIf(e -> e.getDestination().equals(destNode));
          nonFounds.add(edge);
        }
      }

      E nextShortest = nonFounds.poll();
      if (nextShortest == null) {
        // no path to end was found if we've tried everything
        return ImmutableList.of();
      }
      if (nextShortest.getDestination().equals(end)) {
        // stop searching one we hit the end node (it's guaranteed to be
        // the shortest path to that node by Dikstra's algorithm.).
        return buildPathBetween(start, end, prevs);
      } else {
        // find all neighbors off the shortest edge from the known list
        // (note that that edge is now considered 'found', i.e. completely
        // searched)
        curStart = nextShortest.getDestination();
      }
    }
  }

  /**
   * Using a map between nodes and their predecessors, builds a list of edges
   * from start to end.
   */
  private List<E> buildPathBetween(N start, N end, Map<N, E> prevs) {
    List<E> path = new LinkedList<>();
    E prev = prevs.get(end);

    while (!prev.getSource().equals(start)) {
      path.add(0, prev);
      if (!prevs.containsKey(prev.getSource())) {
        // no path from start to edge
        return ImmutableList.of();
      }
      prev = prevs.get(prev.getSource());
    }

    path.add(0, prev); // add last one going to start
    return path;
  }

  /**
   * A comparator to compare two edges by their NET distance from a root node.
   *
   * @author mcisler
   */
  private class ShortestComparator implements Comparator<E> {
    private Map<N, Double> shortest;

    /**
     * @param shortest
     *          A map from node to the shortest known distance to it.
     */
    ShortestComparator(Map<N, Double> shortest) {
      this.shortest = shortest;
    }

    @Override
    public int compare(E o1, E o2) {
      return shortest.get(o1.getDestination())
          .compareTo(shortest.get(o2.getDestination()));
    }
  }
}
