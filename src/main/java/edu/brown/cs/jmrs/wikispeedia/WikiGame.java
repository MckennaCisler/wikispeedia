package edu.brown.cs.jmrs.wikispeedia;

import java.util.Set;

import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * A basic object representing a particular WikiGame.
 *
 * @author mcisler
 *
 */
public class WikiGame {
  private final WikiPage      start;
  private final WikiPage      goal;
  private final Set<WikiPage> space;

  /**
   * @param start
   *          The starting page of this game.
   * @param goal
   *          The goal page of this game.
   * @param space
   *          The space of pages surrounding the start and goal in this game.
   *          Presumed to be found during generation, but with no guarantees.
   */
  public WikiGame(WikiPage start, WikiPage goal, Set<WikiPage> space) {
    this.start = start;
    this.goal = goal;
    this.space = space;
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
   * @return The space of pages surrounding the start and goal in this game.
   *         Presumed to be found during generation, but with no guarantees.
   */
  public final Set<WikiPage> getSpace() {
    return space;
  }
}
