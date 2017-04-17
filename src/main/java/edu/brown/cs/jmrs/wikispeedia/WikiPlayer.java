package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * A player in a Wiki lobby, storing state information about their curPage page
 * and place in the game.
 *
 * @author mcisler
 *
 */
public class WikiPlayer {
  /**
   * Identifiers.
   */
  private final String id;
  private final String name;

  private final WikiLobby lobby;

  /**
   * Time measurements (endTime is only set upon completion).
   */
  private Instant endTime;

  /**
   * Path and location data.
   */
  private List<WikiPage> path;
  private final WikiPage startPage;
  private final WikiPage goalPage;
  private WikiPage curPage;

  /**
   * @param id
   *          This player's unique ID.
   * @param name
   *          This player's name.
   * @param lobby
   *          The lobby this player is in.
   * @param startPage
   *          The starting page of this player.
   * @param goalPage
   *          The page this player is trying to get to.
   */
  public WikiPlayer(String id, String name, WikiLobby lobby, WikiPage startPage,
      WikiPage goalPage) {
    super();
    this.id = id;
    this.name = name;
    this.lobby = lobby;
    this.startPage = startPage;
    this.goalPage = goalPage;
  }

  /****************************************/
  /* GETTERS */
  /****************************************/

  /**
   * @return This player's unique ID.
   */
  public final String getId() {
    return id;
  }

  /**
   * @return This player's name.
   */
  public final String getName() {
    return name;
  }

  /**
   * @return Whether the player has finished a game. Indicated internally by the
   *         state of endTime.
   */
  public boolean isDone() {
    return endTime != null;
  }

  /**
   * @return The time this player started.
   */
  public final Instant getStartTime() {
    return lobby.getStartTime();
  }

  /**
   * @return The time this player finished their game, or null if they have not
   *         finished.
   */
  public final Instant getEndTime() {
    return endTime;
  }

  /**
   * @return The playTime of this player.
   */
  public final Duration getPlayTime() {
    assert endTime != null;
    return Duration.between(lobby.getStartTime(), endTime);
  }

  /**
   * @return The path the player has taken through wiki articles.
   */
  public final List<WikiPage> getPath() {
    return path;
  }

  /**
   * @return The length of this player's path, adjusted for reversals, etc.
   */
  public final int getPathLength() {
    return path.size(); // TODO
  }

  /**
   * @return The page that the player is currently at.
   * @throws IOException
   *           If the current page cannot be accessed.
   */
  public WikiPage getCurPage() throws IOException {
    return curPage;
  }

  /**
   * @return The pages that the player can current go to.
   * @throws IOException
   *           If the current page cannot be accessed.
   */
  public Set<WikiPage> getLinks() throws IOException {
    return lobby.getLinkFinder().linkedPages(curPage);
  }

  /****************************************/
  /* PLAYER STATE HANDLERS */
  /****************************************/

  /**
   * Checks internally whether the player is done.
   *
   * @param endTimeIfSo
   *          The time the player should be considered to have finished if they
   *          did finish.
   * @return Whether the player was done after checking.
   */
  public synchronized boolean checkIfDone(Instant endTimeIfSo) {
    if (isDone()) {
      throw new IllegalStateException(
          "Player " + id + " has already reached goal.");
    }

    if (curPage.equals(goalPage)) {
      this.endTime = endTimeIfSo;
      return true;
    }
    return false;
  }

  /**
   * Sets the endTime for this player. Can only be set once.
   *
   * @param endTime
   *          The endTime to set
   */
  public synchronized void setEndTime(Instant endTime) {
    assert this.endTime == null;
    assert endTime != null;
    this.endTime = endTime;
  }

  /**
   * Attempts to move this player to the given page. If they cannot be moved,
   * return false and keep the player where they are.
   *
   * @param page
   *          The page this player is requesting to go to.
   * @return Whether this player can go to that page from their curPage page.
   * @throws IOException
   *           If the curPage page cannot be accessed.
   */
  public synchronized boolean goToPage(WikiPage page) throws IOException {
    if (isDone()) {
      throw new IllegalStateException(
          "Player " + id + " has already reached goal.");
    }

    // we assume that the curPage page has been cached already (speed issue)
    if (lobby.getLinkFinder().linkedPages(curPage).contains(page)) {
      return path.add(page);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    WikiPlayer other = (WikiPlayer) obj;
    return other.id.equals(this.id);
  }

  @Override
  public String toString() {
    return "Player " + id + " named '" + name + "' in lobby " + lobby + " at "
        + curPage.url();
  }

}
