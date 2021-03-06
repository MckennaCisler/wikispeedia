package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.wikispeedia.comms.Command;

/**
 * A player in a Wiki lobby, storing state information about their curPage page
 * and place in the game.
 *
 * @author mcisler
 *
 */
public class WikiPlayer {
  private static final int MAX_NAME_LENGTH = 30;
  /**
   * Identifiers.
   */
  private final String     id;
  private String           name;

  private transient WikiLobby lobby;

  /**
   * State variables (endTime is only set upon completion).
   */
  private final boolean     isLeader;
  private transient Instant startTime = null;
  private transient Instant endTime   = null;
  private boolean           ready;           // for match starting
  private boolean           connected;

  /**
   * WikiPath and location data (in case where start and end page are different
   * for each player in lobby).
   */
  private final WikiPath path;     // curPage is last page in here
  private final WikiPage startPage;
  private final WikiPage goalPage;

  /**
   * @param id
   *          This player's unique ID.
   * @param lobby
   *          The lobby this player is in.
   * @param isLeader
   *          Whether this player started their lobby.
   */
  public WikiPlayer(String id, WikiLobby lobby, boolean isLeader) {
    this.isLeader = isLeader;
    ready = false;
    connected = true;
    this.id = id;
    this.name = "";
    assert lobby != null;
    this.lobby = lobby;
    this.startPage = lobby.getStartPage();
    this.goalPage = lobby.getGoalPage();
    this.path = new WikiPath(startPage);
  }

  /**
   * @param n
   *          This player's name.
   */
  public void setName(String n) {
    assert n.length() > 0;
    name =
        n.substring(0,
            n.length() > MAX_NAME_LENGTH ? MAX_NAME_LENGTH : n.length());
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
  public final synchronized String getName() {
    return name;
  }

  /**
   * @return This player's name.
   */
  public final WikiLobby getLobby() {
    return lobby;
  }

  /**
   * @return Whether this player is the leader of their lobby.
   */
  public boolean isLeader() {
    return isLeader;
  }

  /**
   * @return Whether the player has finished a game, whether they won or not.
   *         Indicated internally by the state of endTime.
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
   * @return The time the lobby this player is in started, or null if not
   *         started.
   */
  public final Instant getStartTime() {
    assert startTime.equals(lobby.getStartTime());
    return startTime;
  }

  /**
   * @return The time this player finished their game, or null if they have not
   *         finished.
   */
  public final synchronized Instant getEndTime() {
    if (!done()) {
      throw new IllegalStateException("Player not done, endTime will be null");
    }
    return endTime;
  }

  /**
   * @return The playTime of this player, either the complete time or a running
   *         time.
   * @throws IllegalStateException
   *           If the lobby has not been started.
   */
  public synchronized Duration getPlayTime() {
    // done() is equivalent to endTime != null
    assert startTime.equals(lobby.getStartTime());
    return Duration.between(startTime, done() ? getEndTime() : Instant.now());
  }

  /**
   * @return The path the player has taken through wiki articles.
   */
  public final WikiPath getPath() {
    return path;
  }

  /**
   * @return The length of this player's path, adjusted for reversals, etc.
   */
  public final int getPathLength() {
    return path.size();
  }

  /**
   * @return The page that the player is currently at.
   */
  public WikiPage getCurPage() {
    return path.end();
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
  public synchronized void setConnected(boolean state) {
    connected = state;
  }

  /**
   * @param ready
   *          The ready state to set this player to.
   */
  public synchronized void setReady(boolean ready) {
    this.ready = ready;

    // update other players in lobby and possibly start game
    lobby.checkAllReady();
  }

  /**
   * Checks internally whether the player is done.
   *
   * @param endTimeIfSo
   *          The time the player should be considered to have finished if they
   *          did finish.
   * @return Whether the player was done after checking.
   * @throws IOException
   *           If the pages could not be fully accessed for checking.
   */
  synchronized boolean checkIfDone(Instant endTimeIfSo) throws IOException {
    if (done()) {
      return true;
    }
    assert getCurPage() != null;
    assert goalPage != null;

    if (getCurPage().equalsAfterRedirect(goalPage)) {
      this.endTime = endTimeIfSo;
      assert done();
      return true;
    }
    return false;
  }

  /**
   * Sets the startTime for this player. Can only be set once.
   *
   * @param startTime
   *          The startTime to set
   */
  void setStartTime(Instant startTime) {
    assert this.startTime == null;
    assert startTime != null;
    assert startTime.equals(lobby.getStartTime());
    this.startTime = startTime;
    path.setStartTime(startTime);
  }

  /**
   * Sets the endTime for this player. Can only be set once.
   *
   * @param endTime
   *          The endTime to set
   */
  synchronized void setEndTime(Instant endTime) {
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
    checkLobbyState();

    // we assume that the curPage page has been cached already (speed issue)
    if (lobby.getLinkFinder().linkedPages(getCurPage()).contains(page)) {
      if (!page.equalsAfterRedirect(getCurPage())) {
        path.add(page);
      }
      checkIfDone(Instant.now());

      // upon success, check for winner in lobby and notify people
      lobby.checkForWinner();
      Command.sendAllPlayers(lobby);
      return true;
    }
    return false;
  }

  /**
   * @param page
   *          The page in the player's history to go to
   * @throws IOException
   *           If there was no previous page or the given page was not in the
   *           player's history
   */
  public synchronized void goBackPage(WikiPage page) throws IOException {
    checkLobbyState();

    if (!path.contains(page)) {
      // they may be moving forward, so try to go to the page from this one
      if (goToPage(page)) {
        return;
      }
      throw new NoSuchElementException(String.format(
          "Page %s neither in player's history nor available ahead",
          page.getName()));
    }

    WikiPage prevPage;
    int i = path.size() - 1;
    do {
      prevPage = path.get(i--).getPage();
    } while (i >= 0 && !prevPage.equalsAfterRedirectSafe(page));

    // add the found prevPage to the path
    path.add(prevPage);

    // if by some magic we've become done, note it
    checkIfDone(Instant.now());

    // let people know
    Command.sendAllPlayers(lobby);
  }

  private void checkLobbyState() {
    if (!lobby.started()) {
      throw new IllegalStateException("Lobby has not started");
    } else if (lobby.ended()) {
      throw new IllegalStateException("Lobby has ended");
    } else if (done()) {
      throw new IllegalStateException(
          "Cannot move; goal has already been reached");
    }
  }

  /**
   * Custom serializer for use with GSON.
   *
   * @author mcisler
   *
   */
  public static class Serializer implements JsonSerializer<WikiPlayer> {

    @Override
    public JsonElement serialize(WikiPlayer src, Type typeOfSrc,
        JsonSerializationContext context) {
      JsonObject root = new JsonObject();
      root.addProperty("id", src.getId());
      root.addProperty("name", src.getName());
      root.addProperty("isLeader", src.isLeader());
      root.addProperty("ready", src.ready());
      root.addProperty("connected", src.connected());
      root.addProperty("done", src.done());
      root.add("startPage", Main.GSON.toJsonTree(src.startPage));
      root.add("goalPage", Main.GSON.toJsonTree(src.goalPage));
      root.add("path", Main.GSON.toJsonTree(src.path));
      if (src.getLobby().started()) {
        root.addProperty("startTime", src.getStartTime().toEpochMilli());
        root.addProperty("playTime", src.getPlayTime().toMillis());
      }
      if (src.done()) {
        root.addProperty("endTime", src.getEndTime().toEpochMilli());
      }
      if (src.getLobby().ended()) {
        root.addProperty("isWinner", src.getLobby().getWinners().contains(src));
      }

      return root;
    }
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
    return String.format("Player %s (%s) named '%s' in lobby %s at %s", id,
        connected() ? "connected" : "disconnected", name, lobby,
        getCurPage() == null ? "[no page yet]" : getCurPage().url());
  }
}
