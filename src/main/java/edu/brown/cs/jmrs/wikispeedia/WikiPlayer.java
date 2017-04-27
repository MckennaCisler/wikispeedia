package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
  private String name;

  private transient WikiLobby lobby;

  /**
   * State variables (endTime is only set upon completion).
   */
  private transient Instant endTime;
  private boolean ready; // for match starting
  private boolean connected;

  /**
   * Path and location data (in case where start and end page are different for
   * each player in lobby).
   */
  private final List<WikiPage> path; // curPage is last page in here
  private final WikiPage startPage;
  private final WikiPage goalPage;

  /**
   * @param id
   *          This player's unique ID.
   * @param lobby
   *          The lobby this player is in.
   * @param startPage
   *          The starting page of this player.
   * @param goalPage
   *          The page this player is trying to get to.
   */
  public WikiPlayer(String id, WikiLobby lobby, WikiPage startPage,
      WikiPage goalPage) {
    super();
    ready = false;
    connected = true;
    this.id = id;
    this.name = "";
    this.lobby = lobby;
    this.startPage = startPage;
    this.goalPage = goalPage;
    this.path = new ArrayList<>();
    this.path.add(startPage);
  }

  /**
   * @param n
   *          This player's name.
   */
  public void setName(String n) {
    name = n;
  }

  /****************************************/
  /* GETTERS */
  /****************************************/

  /**
   * @return Whether this player is ready to start.
   */
  public final boolean ready() {
    return ready;
  }

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
  public boolean done() {
    return endTime != null;
  }

  /**
   * @return Whether the player client is currently connected.
   */
  public boolean connected() {
    return connected;
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
    return path.size(); // TODO:
  }

  /**
   * @return The page that the player is currently at.
   */
  public WikiPage getCurPage() {
    return path.get(path.size() - 1);
  }

  /**
   * @return The pages that the player can current go to.
   * @throws IOException
   *           If the current page cannot be accessed.
   */
  public Set<WikiPage> getLinks() throws IOException {
    return lobby.getLinkFinder().linkedPages(getCurPage());
  }

  /****************************************/
  /* PLAYER STATE HANDLERS */
  /****************************************/

  /**
   * @param state
   *          The connected state to set the player to.
   */
  public void setConnected(boolean state) {
    connected = state;
  }

  /**
   * @param ready
   *          The ready state to set this player to.
   */
  public synchronized void setReady(boolean ready) {
    this.ready = ready;
  }

  /**
   * Checks internally whether the player is done.
   *
   * @param endTimeIfSo
   *          The time the player should be considered to have finished if they
   *          did finish.
   * @return Whether the player was done after checking.
   */
  private synchronized boolean checkIfDone(Instant endTimeIfSo) {
    if (done()) {
      throw new IllegalStateException(
          String.format("Player %s has already reached the goal", name));
    }

    if (getCurPage().equals(goalPage)) {
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
    // if (!lobby.started()) { // TODO:
    // throw new IllegalStateException(
    // String.format("Player %s's lobby has not started", name));
    // } else
    if (lobby.ended()) {
      throw new IllegalStateException(
          String.format("Player %s's lobby has ended", name));
    } else if (done()) {
      throw new IllegalStateException(
          String.format("Player %s has already reached the goal", name));
    }

    // we assume that the curPage page has been cached already (speed issue)
    if (lobby.getLinkFinder().linkedPages(getCurPage()).contains(page)) {
      if (!page.equals(getCurPage())) {
        path.add(page);
      }
      checkIfDone(Instant.now());
      return true;
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
    return String.format("Player %s named '%s' in lobby %s at %s", id, name,
        lobby, getCurPage().url());
  }
}
