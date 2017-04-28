package edu.brown.cs.jmrs.wikispeedia;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.web.ContentFormatter;
import edu.brown.cs.jmrs.web.ContentFormatterChain;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiAnnotationRemover;
import edu.brown.cs.jmrs.web.wikipedia.WikiBodyFormatter;
import edu.brown.cs.jmrs.web.wikipedia.WikiFooterRemover;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder.Filter;

/**
 * Coordinates a lobby of players in a Wiki game.
 *
 * @author mcisler
 *
 */
public class WikiLobby implements Lobby {

  private static final LinkFinder<WikiPage> DEFAULT_LINK_FINDER =
      new WikiPageLinkFinder(Filter.DISAMBIGUATION);

  private static final ContentFormatter<WikiPage> DEFAULT_CONTENT_FORMATTER =
      new ContentFormatterChain<WikiPage>(
          ImmutableList.of(new WikiBodyFormatter(), new WikiFooterRemover(),
              new WikiAnnotationRemover()));

  private transient Server server;
  private final String id;
  // map from id to player
  private transient Map<String, WikiPlayer> players;
  private transient LinkFinder<WikiPage> linkFinder;
  private transient ContentFormatter<WikiPage> contentFormatter;
  private Instant startTime = null;
  private Instant endTime = null;

  private WikiPage startPage;
  private WikiPage goalPage;

  private WikiPlayer winner;

  /**
   * Constructs a new WikiLobby (likely through a Factory in
   * {@link edu.brown.cs.jmrs.server.Server}.
   *
   * @param server
   *          The server it was called from.
   * @param id
   *          The id of this lobby.
   */
  public WikiLobby(Server server, String id) {
    this.server = server;
    this.id = id;
    players = new HashMap<>();
    this.linkFinder = DEFAULT_LINK_FINDER;
    this.contentFormatter = DEFAULT_CONTENT_FORMATTER;
  }

  /****************************************/
  /* LOBBY OVERRIDES */
  /****************************************/

  @Override
  public void init(JsonObject arguments) {
    WikiGame game = GameGenerator.ofDist(10); // TODO
    this.startPage = game.getStart();
    this.goalPage = game.getGoal();
  }

  @Override
  public boolean isClosed() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void addClient(String playerId) {
    if (started()) {
      throw new IllegalStateException(
          "Game has already started, cannot add player.");
    }
    this.players.put(playerId,
        new WikiPlayer(playerId, this, startPage, goalPage));
    Command.sendAllPlayers(this);
  }

  @Override
  public void removeClient(String playerId) {
    this.players.remove(playerId);
    Command.sendAllPlayers(this);
  }

  @Override
  public void playerReconnected(String clientId) {
    players.get(clientId).setConnected(true);
    Command.sendAllPlayers(this);
  }

  @Override
  public void playerDisconnected(String clientId) {
    players.get(clientId).setConnected(false);
    Command.sendAllPlayers(this);
  }
  
  @Override
  public JsonElement toJson(Gson gson) {
	  return gson.toJsonTree(this);
  }

  /****************************************/
  /* STATE HANDLERS / SETTERS */
  /****************************************/

  /**
   * @param page
   *          The starting page of players in this lobby.
   */
  public void setStartPage(WikiPage page) {
    this.startPage = page;
  }

  /**
   * @param page
   *          The page players in this lobby are trying to get to.
   */
  public void setGoalPage(WikiPage page) {
    this.goalPage = page;
  }

  /**
   * @param linkFinder
   *          The linkFinder to use when showing / letting players move through
   *          pages.
   */
  public void setLinkFinder(LinkFinder<WikiPage> linkFinder) {
    this.linkFinder = linkFinder;
  }

  /**
   * @param playerId
   *          The id of the player to set the name of.
   * @param name
   *          The name to set.
   */
  public void setPlayerName(String playerId, String name) {
    players.get(playerId).setName(name);
  }

  /**
   * @param contentFormatter
   *          The ContentFormatter associated with this lobby, used to reformat
   *          the parsedContent() of player's Wikipages.
   */
  public void setContentFormatter(ContentFormatter<WikiPage> contentFormatter) {
    this.contentFormatter = contentFormatter;
  }

  /**
   * Start the game by setting a start time. Players cannot join after this.
   *
   * @throws IllegalStateException
   *           if a player is not ready.
   */
  public void start() {
    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      if (!entry.getValue().ready()) {
        throw new IllegalStateException(String.format("Player %s is not ready",
            entry.getValue().getName()));
      }
    }
    startTime = Instant.now(); // this is how we determine whether started
  }

  /**
   * Stops the game by setting an end time and configuring all players.
   */
  public void stop() {
    endTime = Instant.now();
    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      if (!entry.getValue().done()) {
        entry.getValue().setEndTime(endTime);
      }
    }
  }

  /**
   * Sends a message to all players with the states of all other players. Also
   * initiates game start.
   *
   * @return Whether all were ready and the game is starting.
   */
  public boolean checkAllReady() {
    boolean allReady = true;
    Map<String, Boolean> playerStates = new HashMap<>(players.size());
    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      boolean ready = entry.getValue().ready();
      if (!ready) {
        allReady = false;
      }
      playerStates.put(entry.getKey(), ready);
    }

    // notify with new states (state)
    Command.sendAllPlayers(this);

    if (allReady) {
      Command.sendBeginGame(this);
    }

    return allReady;
  }

  /**
   * Checks for a winning player based on each's position. Should be called
   * periodically, or at least at every player move. Note that winners are not
   * determined here, so the race condition occurs elsewhere if two are close.
   *
   * @return Whether there was a winner.
   */
  public boolean checkForWinner() {
    List<WikiPlayer> done = new ArrayList<>(1);
    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      if (entry.getValue().done()) {
        done.add(entry.getValue());
      }
    }

    if (done.size() > 0) {
      if (done.size() > 1) {
        // sort by play time TODO: what about shortest path?
        done.sort((p1, p2) -> p1.getPlayTime().compareTo(p2.getPlayTime()));
      }

      winner = done.get(0);
      stop();

      Command.sendEndGame(this);
      return true;
    }
    return false;
  }

  /****************************************/
  /* GETTERS */
  /****************************************/

  /**
   * @return The LinkFinder associated with this lobby, used in finding links
   *         from WikiPages.
   */
  public LinkFinder<WikiPage> getLinkFinder() {
    return linkFinder;
  }

  /**
   * @return The ContentFormatter associated with this lobby, used to reformat
   *         the parsedContent() of player's Wikipages.
   */
  public ContentFormatter<WikiPage> getContentFormatter() {
    return contentFormatter;
  }

  /**
   * @return The start wiki page of this lobby.
   */
  public WikiPage getStartPage() {
    return startPage;
  }

  /**
   * @return The goal wiki page of this lobby.
   */
  public WikiPage getGoalPage() {
    return goalPage;
  }

  /**
   * @return The server associated with this lobby.
   */
  public Server getServer() {
    return server;
  }

  /**
   * @return The players in this map.
   */
  public List<WikiPlayer> getPlayers() {
    return ImmutableList.copyOf(players.values());
  }

  /**
   * @param playerId
   *          The id of the player to get.
   * @return The player with that ID, or null if none found.
   */
  public WikiPlayer getPlayer(String playerId) {
    return players.get(playerId);
  }

  /**
   * @return The player who won the game.
   * @throws IllegalStateException
   *           If the game has not ended.
   */
  public WikiPlayer getWinner() {
    if (!ended()) {
      throw new IllegalStateException("Lobby has not ended.");
    }
    return winner;
  }

  /**
   * @return Whether the lobby has started the game.
   */
  public boolean started() {
    return startTime != null;
  }

  /**
   * @return Whether the lobby has ended, either due to cancelling or a player
   *         winning.
   */
  public boolean ended() {
    return endTime != null;
  }

  /**
   * @return The current time the lobby has been started, i.e. the duration
   *         since startTime.
   * @throws IllegalStateException
   *           If the lobby has not been started.
   */
  public Duration getPlayTime() {
    if (!started()) {
      throw new IllegalStateException("Lobby not started.");
    }
    return Duration.between(startTime, Instant.now());
  }

  /**
   * @return When this lobby was started.
   * @throws IllegalStateException
   *           If the lobby has not been started.
   */
  public Instant getStartTime() {
    if (!started()) {
      throw new IllegalStateException("Lobby not started.");
    }
    return startTime;
  }

  /**
   * @return When this lobby ended.
   * @throws IllegalStateException
   *           If the lobby has not ended or was not started.
   */
  public Instant getEndTime() {
    if (!ended()) {
      throw new IllegalStateException("Lobby has not ended.");
    }
    return endTime;
  }

  /**
   * Custom serializer for use with GSON.
   *
   * @author mcisler
   *
   */
  public static class Serializer implements JsonSerializer<WikiLobby> {

    @Override
    public JsonElement serialize(WikiLobby src, Type typeOfSrc,
        JsonSerializationContext context) {
      JsonObject lobby = new JsonObject();

      lobby.addProperty("id", src.id);
      lobby.add("startPage", Main.GSON.toJsonTree(src.getStartPage()));
      lobby.add("goalPage", Main.GSON.toJsonTree(src.getGoalPage()));
      lobby.addProperty("started", src.started());
      lobby.addProperty("ended", src.ended());
      if (src.started()) {
        lobby.addProperty("startTime", src.getStartTime().toEpochMilli());
        lobby.addProperty("playTime", src.getPlayTime().toMinutes());
      }
      if (src.ended()) {
        lobby.addProperty("endTime", src.getEndTime().toEpochMilli());
        lobby.add("winner", Main.GSON.toJsonTree(src.getWinner()));
      }

      return lobby;
    }
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", id,
        started() ? (ended() ? "ended" : "started") : "not started");
  }
}
