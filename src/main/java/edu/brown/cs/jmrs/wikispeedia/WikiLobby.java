package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.brown.cs.jmrs.collect.Functional;
import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.server.errorhandling.ServerError;
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
import edu.brown.cs.jmrs.wikispeedia.WikiGameMode.Mode;
import edu.brown.cs.jmrs.wikispeedia.comms.Command;
import edu.brown.cs.jmrs.wikispeedia.comms.WikiInterpreter;

/**
 * Coordinates a lobby of players in a Wiki game.
 *
 * @author mcisler
 *
 */
public class WikiLobby implements Lobby {
  /**
   * A class representing a message to a user.
   *
   * @author mcisler
   */
  private class Message {

    private Instant timestamp;
    private String  content;
    private String  senderId;

    /**
     * @param content
     *          The message content
     * @param senderId
     *          The id of the sender
     */
    Message(String content, String senderId) {
      this.content = content;
      this.senderId = senderId;
      timestamp = Instant.now();
    }

    public String getContent() {
      return content;
    }

    public String getSender() {
      return senderId;
    }

    public Instant getTime() {
      return timestamp;
    }
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
      lobby.addProperty("gameMode", src.gameMode.getGameMode().ordinal());
      try {
        lobby.add("startPage",
            WikiInterpreter.getPlayerPageInfo(src.getStartPage(), src));
        lobby.add("goalPage",
            WikiInterpreter.getPlayerPageInfo(src.getGoalPage(), src));
      } catch (IOException e) {
        lobby.add("startPage", Main.GSON.toJsonTree(src.getStartPage()));
        lobby.add("goalPage", Main.GSON.toJsonTree(src.getGoalPage()));
      }
      lobby.addProperty("started", src.started());
      lobby.addProperty("ended", src.ended);
      if (src.started()) {
        lobby.addProperty("startTime", src.getStartTime().toEpochMilli());
        lobby.addProperty("playTime", src.getPlayTime().toMillis());
      }
      if (src.ended) {
        lobby.addProperty("endTime",
            src.getEndTime() != null ? src.getEndTime().toEpochMilli() : null);
        lobby.add("winners", Main.GSON.toJsonTree(src.getWinners()));
      }

      return lobby;
    }
  }

  public static final ContentFormatter<WikiPage> DEFAULT_CONTENT_FORMATTER =
      new ContentFormatterChain<WikiPage>(
          ImmutableList.of(new WikiBodyFormatter(), new WikiFooterRemover(),
              new WikiAnnotationRemover()));
  // NOTE: this should be configured to provide MORE or AT LEAST as many links
  // as the game generation link finder, so that players can definitely go down
  // a path
  public static final LinkFinder<WikiPage> DEFAULT_LINK_FINDER;

  static final boolean USE_CACHING_WIKI_LINK_FINDER = false;
  static final double  CACHING_THREAD_CPU_USAGE     = 0.75;
  static {
    if (USE_CACHING_WIKI_LINK_FINDER) {
      try {
        DEFAULT_LINK_FINDER =
            new CachingWikiLinkFinder(Main.getWikiDbConn(),
                CACHING_THREAD_CPU_USAGE, DEFAULT_CONTENT_FORMATTER,
                Filter.DISAMBIGUATION, Filter.NON_ENGLISH_WIKIPEDIA);
      } catch (SQLException e) {
        throw new AssertionError("Could not initialize wikipedia database", e);
      }
    } else {
      DEFAULT_LINK_FINDER =
          new WikiPageLinkFinder(DEFAULT_CONTENT_FORMATTER,
              Filter.DISAMBIGUATION, Filter.NON_ENGLISH_WIKIPEDIA);
    }
  }
  /**
   * Time to delay lobby creation by.
   */
  private static final long START_DELAY   = 5;
  private static final int  MAX_ID_LENGTH = 30;

  private transient Server server;
  private final String     id;

  // map from id to player
  private transient Map<String, WikiPlayer> players;
  private transient WikiGameMode            gameMode  = null;
  private Instant                           startTime = null;
  private WikiGame                          game;

  private boolean         ended;  // only allow ending once
  private Set<WikiPlayer> winners;

  /****************************************/
  /* LOBBY OVERRIDES */
  /****************************************/

  private List<Message> messages;

  /**
   * Constructs a new WikiLobby (likely through a Factory in
   * {@link edu.brown.cs.jmrs.server.Server}.
   *
   * @param server
   *          The server it was called from.
   * @param id
   *          The id of this lobby. Cut off at a certain number of characters.
   */
  public WikiLobby(Server server, String id) {
    this.server = server;
    assert id.length() > 0;
    this.id =
        id.substring(0,
            id.length() > MAX_ID_LENGTH ? MAX_ID_LENGTH : id.length());
    players = new ConcurrentHashMap<>();
    messages = Collections.synchronizedList(new ArrayList<>());
    winners = ImmutableSet.of();
    ended = false;
  }

  @Override
  public synchronized void addClient(String playerId) {
    if (started()) {
      Command.sendError(server, playerId,
          "Game has already started, cannot add player");
      return;
    }
    // first is leader
    boolean isLeader = players.size() == 0;

    players.put(playerId, new WikiPlayer(playerId, this, isLeader));

    Command.sendAllPlayers(this);
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
        break;
      }
    }

    // notify with new states (state)
    Command.sendAllPlayers(this);

    if (allReady) {
      start(false);
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
  public synchronized boolean checkForWinner() {
    if (!ended) {
      Set<WikiPlayer> possibleWinners = gameMode.checkForWinners(this);
      if (possibleWinners.size() > 0) {
        winners = possibleWinners;
        stop();
        Command.sendEndGame(this);
        return true;
      }
    }
    // there really should be a winner if the lobby has ended
    assert winners != null;
    return winners.size() > 0;
  }

  /**
   * @return All players still connected to this lobby.
   */
  public List<WikiPlayer> getConnectedPlayers() {
    return Functional.filter(new ArrayList<>(players.values()),
        WikiPlayer::connected);
  }

  /**
   * @return All players in this lobby, including those disconnected.
   */
  public List<WikiPlayer> getAllPlayers() {
    return new ArrayList<>(players.values());
  }

  /****************************************/
  /* STATE HANDLERS / SETTERS */
  /****************************************/

  /**
   * @return The ContentFormatter associated with this lobby's GameMode, used to
   *         reformat the parsedContent() of player's Wikipages.
   */
  public ContentFormatter<WikiPage> getContentFormatter() {
    return gameMode.getContentFormatter();
  }

  /**
   * @return The default LinkFinder for all lobbies, setup in this lobby.
   */
  public LinkFinder<WikiPage> getDefaultLinkFinder() {
    return DEFAULT_LINK_FINDER;
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
   * @return The game of this lobby
   */
  public WikiGame getGame() {
    return game;
  }

  /**
   * @return The goal wiki page of this lobby.
   */
  public WikiPage getGoalPage() {
    return game.getGoal();
  }

  /**
   * @return The LinkFinder associated with this lobby's GameMode, used in
   *         finding links from WikiPages.
   */
  public LinkFinder<WikiPage> getLinkFinder() {
    return gameMode.getLinkFinder();
  }

  /**
   * @param playerId
   *          The id of the player to get.
   * @return The player with that ID, or null if none found.
   */
  public WikiPlayer getPlayer(String playerId) {
    return players.get(playerId);
  }

  /****************************************/
  /* GETTERS */
  /****************************************/

  /**
   * @return The current time the lobby has been started, i.e. the duration
   *         since startTime, or the total time the lobby lasted for if it has
   *         finished.
   * @throws IllegalStateException
   *           If the lobby has not been started.
   */
  public Duration getPlayTime() {
    if (!started()) {
      throw new IllegalStateException("Lobby has not started");
    }
    return Duration.between(startTime, ended ? getEndTime() : Instant.now());
  }

  /**
   * @return The server associated with this lobby.
   */
  public Server getServer() {
    return server;
  }

  /**
   * @return The start wiki page of this lobby.
   */
  public WikiPage getStartPage() {
    return game.getStart();
  }

  /**
   * @return When this lobby was started.
   * @throws IllegalStateException
   *           If the lobby has not been started.
   */
  public Instant getStartTime() {
    if (!started()) {
      throw new IllegalStateException("Lobby has not started");
    }
    return startTime;
  }

  /**
   * @return The players who won the game, or none if not ended.
   */
  public Set<WikiPlayer> getWinners() {
    return winners;
  }

  /**
   * Called on lobby creation; structures this lobby to follow a certain game
   * mode.
   *
   * @throws ServerError
   *           If a bad page is specified for start/end.
   */
  @Override
  public void init(JsonObject arguments) throws ServerError {
    Main.debugLog("Generating game...");

    int mode = arguments.get("gameMode").getAsInt();
    if (mode == WikiGameMode.Mode.TIME_TRIAL.ordinal()) {
      gameMode = new TimeTrialGameMode();

    } else if (mode == WikiGameMode.Mode.LEAST_CLICKS.ordinal()) {
      gameMode = new LeastClicksGameMode();

    } else {
      throw new IllegalArgumentException("Invalid game mode specified");
    }

    // generate page from difficulty
    double difficulty = arguments.get("difficulty").getAsDouble();
    WikiPage startPage, endPage;

    // add custom shortcut to set start and end page specifically.
    if (arguments.has("startPage")
        && !arguments.get("startPage").getAsString().equals("")) {
      startPage =
          WikiPage.fromAny(arguments.get("startPage").getAsString(),
              Main.WIKI_PAGE_DOC_CACHE);

      if (!startPage.accessible()) {
        throw new ServerError(String.format(
            "Page %s is not a valid Wikipedia page!", startPage.getName()));
      }
    } else {
      startPage = GameGenerator.pageWithObscurity(difficulty);

    }

    if (arguments.has("goalPage")
        && !arguments.get("goalPage").getAsString().equals("")) {
      endPage =
          WikiPage.fromAny(arguments.get("goalPage").getAsString(),
              Main.WIKI_PAGE_DOC_CACHE);

      if (!endPage.accessible()) {
        throw new ServerError(String.format(
            "Page %s is not a valid Wikipedia page!", endPage.getName()));
      }
    } else {
      endPage = GameGenerator.pageWithObscurity(difficulty);
    }

    game = new WikiGame(startPage, endPage, ImmutableSet.of());

    Main.debugLog(String.format("Generated %s game: %s -> %s",
        mode == WikiGameMode.Mode.TIME_TRIAL.ordinal() ? "time trial"
            : "least clicks",
        game.getStart(), game.getGoal()));
  }

  @Override
  public boolean isClosed() {
    boolean closed = true;

    for (WikiPlayer player : players.values()) {
      if (player.connected()) {
        closed = false;
        break;
      }
    }

    return closed;
  }

  @Override
  public void playerDisconnected(String clientId) {
    if (players.containsKey(clientId)) {
      players.get(clientId).setConnected(false);
      Command.sendAllPlayers(this);
    } else {
      throw new AssertionError(String.format(
          "Unknown player with client id %s disconnected; known are %s",
          clientId, players));
    }
  }

  @Override
  public void playerReconnected(String clientId) {
    if (players.containsKey(clientId)) {
      WikiPlayer player = players.get(clientId);
      // make sure reconnecting players are ended (they really should be)
      if (ended) {
        assert player.done();
      }

      players.get(clientId).setConnected(true);
      Command.sendAllPlayers(this);
    } else {
      throw new AssertionError(String.format(
          "Unknown player with client id %s reconnected; known are %s",
          clientId, players));
    }
  }

  /**
   * @param content
   *          Message content
   * @param clientId
   *          Client id who sent message
   */
  public void registerMessage(String content, String clientId) {
    messages.add(new Message(content, clientId));
  }

  @Override
  public synchronized void removeClient(String playerId) {
    this.players.remove(playerId);
    Command.sendAllPlayers(this);

    boolean closeLobby = true;

    for (WikiPlayer player : players.values()) {
      if (player.connected()) {
        closeLobby = false;
        break;
      }
    }

    if (players.size() == 0 || closeLobby) {
      server.closeLobby(id);
    }
  }

  /**
   * @param clientId
   *          Player id to send to
   */
  public void sendMessagesToPlayer(String clientId) {
    Message[] messageArray = messages.toArray(new Message[] {});
    JsonArray jsonArray = new JsonArray();

    for (Message message : messageArray) {
      JsonObject jsonMessage = new JsonObject();
      jsonMessage.addProperty("timestamp", message.getTime().toEpochMilli());
      jsonMessage.addProperty("sender",
          players.get(message.getSender()).getName());
      jsonMessage.addProperty("sender_id", message.getSender());
      jsonMessage.addProperty("message", message.getContent());
      jsonArray.add(jsonMessage);
    }

    JsonObject responseObject = new JsonObject();
    responseObject.addProperty("command", "return_messages");
    responseObject.add("payload", jsonArray);
    responseObject.addProperty("error_message", "");

    server.sendToClient(clientId, new Gson().toJson(responseObject));
  }

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
   * @param force
   *          Whether to force a start, i.e. ignore non-ready players.
   * @throws IllegalStateException
   *           if a player is not ready.
   */
  public void start(boolean force) {
    if (!force) {
      // only connected players
      for (WikiPlayer player : getConnectedPlayers()) {
        if (!player.ready()) {
          throw new IllegalStateException(
              String.format("Player %s is not ready", player.getName()));
        }
      }
    }
    // this is how we determine whether started
    startTime = Instant.now().plusSeconds(START_DELAY);

    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      entry.getValue().setStartTime(startTime);
    }
  }

  /**
   * @return Whether the lobby has started the game.
   */
  public boolean started() {
    return startTime != null;
  }

  /**
   * @return Whether the lobby has ended.
   */
  public boolean ended() {
    return ended;
  }

  /**
   * Stops the game by setting an end time and configuring all players.
   */
  public void stop() {
    // make sure to only set to ended once
    assert !ended;
    ended = gameMode.ended(this);

    // all players
    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      // !done() equivalent to endTime == null
      if (!entry.getValue().done()) {
        entry.getValue().setEndTime(getEndTime());
      }
      // everybody should be done
      assert entry.getValue().done();
    }

    Main.debugLog(String.format(
        "Lobby %s finished; \n\twinners: %s \n\tplayTime: %s\n\tendTime: %s",
        id, getWinners(), getPlayTime(), getEndTime()));
  }

  @Override
  public JsonElement toJson(Gson gson) {
    return gson.toJsonTree(this);
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", id,
        started() ? (ended ? "ended" : "started") : "not started");
  }

  /**
   * @return The game mode of this lobby.
   */
  public Mode getGameMode() {
    return gameMode.getGameMode();
  }

}
