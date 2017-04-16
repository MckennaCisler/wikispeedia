package edu.brown.cs.jmrs.collect.graph;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;

import com.google.common.collect.ImmutableList;

import edu.brown.cs.jmrs.collect.graph.Graph.Edge;
import edu.brown.cs.jmrs.collect.graph.Graph.Node;
import edu.brown.cs.jmrs.collect.graph.Graph.PathFinder;

/**
 * A PathFinder implementation using Dijkstra's algorithm, with the addition of
 * the A* optimization.
 *
 * @author mcisler
 *
 * @param <N>
 *          The particular node implementation.
 * @param <E>
 *          The particular edge implementation.
 */
public class AStarDirectedPathFinder<N extends Node<N, E>, E extends Edge<N, E>>
    implements PathFinder<N, E> {
  private final BiFunction<N, N, Double> heuristicDistanceFunc;
  private final EdgeFinder<N, E> edgeFinder;

  /**
   * @param edgeFinder
   *          The edge finding method to using in finding shortest paths.
   * @param heuristicDistanceFunc
   *          A function to compute a heuristic (estimated) distance between two
   *          provided nodes, to be used in the A* search.
   */
  public AStarDirectedPathFinder(EdgeFinder<N, E> edgeFinder,
      BiFunction<N, N, Double> heuristicDistanceFunc) {
    this.heuristicDistanceFunc = heuristicDistanceFunc;
    this.edgeFinder = edgeFinder;
  }

  /**
   * @see Graph.PathFinder#shortestPath(Node, Node) Note that this
   *      implementation returns a list implemented as a LinkedList.
   */
  @Override
  public List<E> shortestPath(N start, N end) {
    assert !start.equals(end);

    Map<N, Double> shortestDists = new HashMap<>();
    Map<E, Double> distEstimates = new HashMap<>();
    // make sure nodes are sorted by the lowest distance FROM START, NOT the
    // lowest edge value
    PriorityQueue<E> nonFounds =
        new PriorityQueue<>(new DistsComparator(distEstimates));

    Map<N, E> prevs = new HashMap<>();
    N curStart = start;
    shortestDists.put(curStart, 0.0);
    // distEstimates.put(curStart, heuristicDistanceFunc.apply(curStart, end));

    // this should always return because nonFounds will always eventually be
    // emptied, because it is polled every time but only added if a non-found or
    // closer node is found.
    while (true) {
      // add all the neighbors of start to nonFounds / distEstimates
      Collection<? extends E> startEdges = edgeFinder.edges(curStart);
      for (E edge : startEdges) {
        // this will serve as an assertion that the edges given are directed
        N destNode = edge.getDestination();

        // if we have some path to this node, only update its distance and
        // distEstimates path to if necessary (if it doesn't exist or is closer
        // to the root) (use doubles because it will offer the most precision
        // and range of values)
        double edgeDist =
            shortestDists.get(curStart) + edge.getValue().doubleValue();
        if (!shortestDists.containsKey(destNode)
            || Double.compare(edgeDist, shortestDists.get(destNode)) < 0) {
          shortestDists.put(destNode, edgeDist);
          distEstimates.put(edge,
              edgeDist + heuristicDistanceFunc.apply(destNode, end));
          prevs.put(destNode, edge);
          nonFounds.add(edge);
        }
      }

      if (nonFounds.isEmpty()) {
        // no path to end was found if we've tried everything
        return ImmutableList.of();
      }
      E nextShortest = nonFounds.poll();
      if (nextShortest.getDestination().equals(end)) {
        // stop searching one we hit the end node (it's guaranteed to be
        // the distEstimates path to that node by Dikstra's algorithm.).
        return buildPathBetween(start, end, prevs);
      } else {
        // find all neighbors off the distEstimates edge from the known list
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
  private class DistsComparator implements Comparator<E> {
    private Map<E, Double> distEstimates;

    /**
     * @param distEstimates
     *          A map from node to it's estimated distance to the end, using the
     *          heuristic function.
     */
    DistsComparator(Map<E, Double> distEstimates) {
      this.distEstimates = distEstimates;
    }

    @Override
    public int compare(E o1, E o2) {
      return distEstimates.get(o1).compareTo(distEstimates.get(o2));
    }
  }
}
