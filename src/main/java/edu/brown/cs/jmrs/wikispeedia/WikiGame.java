package edu.brown.cs.jmrs.wikispeedia;

import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * A basic object representing a particular WikiGame.
 *
 * @author mcisler
 *
 */
public class WikiGame {
  private final WikiPage start;
  private final WikiPage goal;
  // private final List<WikiPage> knownPath;
  // private final Future<List<WikiPage>> shortestPath;

  /**
   * @param start
   *          The starting page of this game.
   * @param goal
   *          The goal page of this game.
   * @param knownPath
   *          A known path from start to goal.
   * @param shortestPath
   *          A value which may eventually represent the shortest path between
   *          start and goal.
   */
  public WikiGame(WikiPage start, WikiPage goal) {
    // List<WikiPage> knownPath, Future<List<WikiPage>> shortestPath
    super();
    this.start = start;
    this.goal = goal;
    // this.shortestPath = shortestPath;
  }

  /**
   * @return The starting page of this game
   */
  public final WikiPage getStart() {
    return start;
  }

  /**
   * @return The goal page of this game
   */
  public final WikiPage getGoal() {
    return goal;
  }

  /**
   * @return A Future which may eventually represent the shortest path between
   *         start and goal.
   */
  // public final Future<List<WikiPage>> getShortestPath() {
  // return shortestPath;
  // }
}
