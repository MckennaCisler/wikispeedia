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

  /**
   * @param start
   *          The starting page of this game.
   * @param goal
   *          The goal page of this game.
   */
  public WikiGame(WikiPage start, WikiPage goal) {
    super();
    this.start = start;
    this.goal = goal;
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
}
