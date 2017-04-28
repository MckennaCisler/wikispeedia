package edu.brown.cs.jmrs.wikispeedia;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.web.ContentFormatter;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;

/**
 * Coordinates a lobby of players in a Wiki game.
 *
 * @author mcisler
 *
 */
public class WikiLobby implements Lobby {

  private transient Server server;
  private final String id;
  // map from id to player
  private transient Map<String, WikiPlayer> players;
  private transient WikiGameMode gameMode = null;

  private Instant startTime = null;

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
  }

  /****************************************/
  /* LOBBY OVERRIDES */
  /****************************************/

  /**
   * Called on lobby creation; stuctures this lobby to follow a certain game
   * mode.
   */
  @Override
  public void init(JsonObject arguments) {
    int mode = arguments.get("gameMode").getAsInt();
    if (mode == WikiGameMode.Mode.TIME_TRIAL.ordinal()) {
      gameMode = new TimeTrialGameMode();

    } else if (mode == WikiGameMode.Mode.LEAST_CLICKS.ordinal()) {
      gameMode = new LeastClicksGameMode();
      throw new IllegalArgumentException("NOT IMPLEMENTED YET.");

    } else {
      throw new IllegalArgumentException("Invalid GameMode specified.");
    }

    if (arguments.has("startPage") && arguments.has("goalPage")) {
      // add custom shortcut to set start and end page.
      int difficulty = arguments.get("difficulty").getAsInt();
      WikiGame game = GameGenerator.ofDist(difficulty * 2);
      this.startPage = game.getStart();
      this.goalPage = game.getGoal();
    }
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

  /****************************************/
  /* STATE HANDLERS / SETTERS */
  /****************************************/

  /**
   * Sets the player's name, possibly considering other player's names.
   *
   * @param clientId
   *          The player id of the player to set.
   * @param uname
   *          The username to set.
   */
  public void setPlayerName(String clientId, String uname) {
    players.get(clientId).setName(uname);
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
    assert gameMode.ended(this);
    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      if (!entry.getValue().done()) {
        entry.getValue().setEndTime(getEndTime());
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
    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      boolean ready = entry.getValue().ready();
      if (!ready) {
        allReady = false;
      }
    }

    // notify with new states (state)
    Command.sendAllPlayers(this);

    if (allReady) {
      Command.sendBeginGame(this);
    }

    return allReady;
  }

  /**
   * Checks for a winning player based on each's position in a lobby. Should be
   * called periodically, or at least at every player move. Note that winners
   * are not determined here, so the race condition occurs elsewhere if two are
   * close.
   *
   * @return Whether there was a winner.
   */
  public boolean checkForWinner() {
    Optional<WikiPlayer> possibleWinner = gameMode.checkForWinner(this);
    if (possibleWinner.isPresent()) {
      winner = possibleWinner.get();
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
   * @return The LinkFinder associated with this lobby's GameMode, used in
   *         finding links from WikiPages.
   */
  public LinkFinder<WikiPage> getLinkFinder() {
    return gameMode.getLinkFinder();
  }

  /**
   * @return The ContentFormatter associated with this lobby's GameMode, used to
   *         reformat the parsedContent() of player's Wikipages.
   */
  public ContentFormatter<WikiPage> getContentFormatter() {
    return gameMode.getContentFormatter();
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
  Server getServer() {
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
   * @return Whether the lobby has ended, based on its internal game mode.
   */
  public boolean ended() {
    return gameMode.ended(this);
  }

  /**
   * @return The current time the lobby has been started, i.e. the duration
   *         since startTime, or the total time the lobby lasted for if it has
   *         finished.
   * @throws IllegalStateException
   *           If the lobby has not been started.
   */
  public Duration getPlayTime() {
    if (!started()) {
      throw new IllegalStateException("Lobby not started.");
    }
    return Duration.between(startTime, ended() ? getEndTime() : Instant.now());
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
    return gameMode.getEndTime(this);
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
