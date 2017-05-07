package edu.brown.cs.jmrs.wikispeedia;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.brown.cs.jmrs.io.db.DbConn;
import edu.brown.cs.jmrs.server.InputError;
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
import edu.brown.cs.jmrs.wikispeedia.comms.Command;

/**
 * Coordinates a lobby of players in a Wiki game.
 *
 * @author mcisler
 *
 */
public class WikiLobby implements Lobby {
  static final ContentFormatter<WikiPage> DEFAULT_CONTENT_FORMATTER = new ContentFormatterChain<WikiPage>(
      ImmutableList.of(
          new WikiBodyFormatter(),
          new WikiFooterRemover(),
          new WikiAnnotationRemover()));

  static final LinkFinder<WikiPage>       DEFAULT_LINK_FINDER;
  static {
    // try {
    DEFAULT_LINK_FINDER = new WikiPageLinkFinder(
        Filter.DISAMBIGUATION,
        Filter.NON_ENGLISH_WIKIPEDIA);
    // new CachingWikiLinkFinder(Main.getWikiDbConn(), Filter.DISAMBIGUATION,
    // Filter.NON_ENGLISH_WIKIPEDIA);
    // } catch (SQLException e) {
    // throw new AssertionError("Could not initialize wikipedia database", e);
    // }
  }

  /**
   * Time to delay lobby creation by.
   */
  private static final long                 START_DELAY = 5;

<<<<<<< 3990a2668954d16f9ed1f3878be6ce13102e40b2
  private transient Server server;
  private final String     id;
  // map from id to player
  private transient Map<String, WikiPlayer> players;
  private transient WikiGameMode            gameMode = null;

  private Instant         startTime = null;
  private WikiGame        game;
  private Set<WikiPlayer> winners;
  private List<Message>   messages;
=======
  private transient Server                  server;
  private final String                      id;
  // map from id to player
  private transient Map<String, WikiPlayer> players;
  private transient WikiGameMode            gameMode    = null;

  private Instant                           startTime   = null;
  private WikiGame                          game;
  private Set<WikiPlayer>                   winners;
  private List<Message>                     messages;
>>>>>>> documentation

  /**
   * Constructs a new WikiLobby (likely through a Factory in
   * {@link edu.brown.cs.jmrs.server.Server}.
   *
   * @param server
   *          The server it was called from.
   * @param id
   *          The id of this lobby.
   * @param wikiDbConn
   *          The database connection to the wikipedia database.
   */
  public WikiLobby(Server server, String id, DbConn wikiDbConn) {
    this.server = server;
    this.id = id;
    players = new HashMap<>();
    messages = Collections.synchronizedList(new ArrayList<>());
  }

  /****************************************/
  /* LOBBY OVERRIDES */
  /****************************************/

  /**
   * Called on lobby creation; structures this lobby to follow a certain game
   * mode.
   *
   * @throws InputError
   *           If a bad page is specified for start/end.
   */
  @Override
  public void init(JsonObject arguments) throws InputError {
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
    if (arguments.has("startPage")) {
      startPage = WikiPage.fromAny(
          arguments.get("startPage").getAsString(),
          Main.WIKI_PAGE_DOC_CACHE);

      if (!startPage.accessible()) {
        throw new InputError(
            String.format(
                "Page %s is not a valid Wikipedia page!",
                startPage.getName()));
      }
    } else {
      startPage = GameGenerator.pageWithObscurity(difficulty);

    }

    if (arguments.has("goalPage")) {
      endPage = WikiPage.fromAny(
          arguments.get("goalPage").getAsString(),
          Main.WIKI_PAGE_DOC_CACHE);

      if (!endPage.accessible()) {
        throw new InputError(
            String.format(
                "Page %s is not a valid Wikipedia page!",
                endPage.getName()));
      }
    } else {
      endPage = GameGenerator.pageWithObscurity(difficulty);
    }

    game = new WikiGame(startPage, endPage);

    Main.debugLog(
        String.format(
            "Generated %s game: %s -> %s",
            mode == WikiGameMode.Mode.TIME_TRIAL.ordinal() ? "time trial"
                : "least clicks",
            game.getStart(),
            game.getGoal()));
  }

  @Override
  public boolean isClosed() {
    return players.isEmpty();
  }

  @Override
  public void addClient(String playerId) {
    if (started()) {
      Command.sendError(
          server,
          playerId,
          "Game has already started, cannot add player");
      return;
    }
    // first is leader
    boolean isLeader = this.players.size() == 0;

    this.players.put(playerId, new WikiPlayer(playerId, this, isLeader));

    Command.sendAllPlayers(this);
  }

  @Override
  public void removeClient(String playerId) {
    this.players.remove(playerId);
    Command.sendAllPlayers(this);

    if (players.size() == 0) {
      server.closeLobby(id);
    }
  }

  @Override
  public void playerReconnected(String clientId) {
    if (players.containsKey(clientId)) {
      players.get(clientId).setConnected(true);
      Command.sendAllPlayers(this);
    }
  }

  @Override
  public void playerDisconnected(String clientId) {
    if (players.containsKey(clientId)) {
      players.get(clientId).setConnected(false);
      Command.sendAllPlayers(this);
    }
  }

  @Override
  public JsonElement toJson(Gson gson) {
    return gson.toJsonTree(this);
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

  public void registerMessage(String content, String clientId) {
    messages.add(new Message(content, clientId));
  }

  public void sendMessagesToPlayer(String clientId) {
    Message[] messageArray = messages.toArray(new Message[] {});
    JsonArray jsonArray = new JsonArray();

    for (Message message : messageArray) {
      JsonObject jsonMessage = new JsonObject();
      jsonMessage.addProperty("timestamp", message.getTime().toEpochMilli());
      jsonMessage.addProperty("sender", players.get(clientId).getName());
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
   * Start the game by setting a start time. Players cannot join after this.
   *
   * @param force
   *          Whether to force a start, i.e. ignore non-ready players.
   * @throws IllegalStateException
   *           if a player is not ready.
   */
  public void start(boolean force) {
    if (!force) {
      for (Entry<String, WikiPlayer> entry : players.entrySet()) {
        if (!entry.getValue().ready()) {
          throw new IllegalStateException(
              String.format(
                  "Player %s is not ready",
                  entry.getValue().getName()));
        }
      }
    }
    // this is how we determine whether started
    startTime = Instant.now().plusSeconds(START_DELAY);

    // notify players
    for (Entry<String, WikiPlayer> entry : players.entrySet()) {
      entry.getValue().setStartTime(startTime);
    }
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

    Main.debugLog(
        String.format(
            "Lobby %s finished; \n\twinners: %s \n\tplayTime: %s\n\tendTime: %s",
            id,
            getWinners(),
            getPlayTime(),
            getEndTime()));
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
  public boolean checkForWinner() {
    Set<WikiPlayer> possibleWinners = gameMode.checkForWinners(this);
    if (possibleWinners.size() > 0) {
      winners = possibleWinners;
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
   * @return The default LinkFinder for all lobbies, setup in this lobby.
   */
  public LinkFinder<WikiPage> getDefaultLinkFinder() {
    return DEFAULT_LINK_FINDER;
  }

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
   * @return The game of this lobby
   */
  public WikiGame getGame() {
    return game;
  }

  /**
   * @return The start wiki page of this lobby.
   */
  public WikiPage getStartPage() {
    return game.getStart();
  }

  /**
   * @return The goal wiki page of this lobby.
   */
  public WikiPage getGoalPage() {
    return game.getGoal();
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
  public Set<WikiPlayer> getWinners() {
    if (!ended()) {
      throw new IllegalStateException("Lobby has not ended");
    }
    return winners;
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
      throw new IllegalStateException("Lobby has not started");
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
      throw new IllegalStateException("Lobby has not started");
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

  private class Message {

    private Instant timestamp;
    private String  content;
    private String  senderId;

    public Message(String content, String senderId) {
      this.content = content;
      this.senderId = senderId;
      timestamp = Instant.now();
    }

    public Instant getTime() {
      return timestamp;
    }

    public String getSender() {
      return senderId;
    }

    public String getContent() {
      return content;
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
    public JsonElement serialize(
        WikiLobby src,
        Type typeOfSrc,
        JsonSerializationContext context) {
      JsonObject lobby = new JsonObject();

      lobby.addProperty("id", src.id);
      lobby.add("startPage", Main.GSON.toJsonTree(src.getStartPage()));
      lobby.addProperty("gameMode", src.gameMode.getGameMode().ordinal());
      lobby.add("goalPage", Main.GSON.toJsonTree(src.getGoalPage()));
      lobby.addProperty("started", src.started());
      lobby.addProperty("ended", src.ended());
      if (src.started()) {
        lobby.addProperty("startTime", src.getStartTime().toEpochMilli());
        lobby.addProperty("playTime", src.getPlayTime().toMillis());
      }
      if (src.ended()) {
        lobby.addProperty("endTime", src.getEndTime().toEpochMilli());
        lobby.add("winners", Main.GSON.toJsonTree(src.getWinners()));
        // TODO: Shortest / known path
      }

      return lobby;
    }
  }

  @Override
  public String toString() {
    return String.format(
        "%s (%s)",
        id,
        started() ? (ended() ? "ended" : "started") : "not started");
  }

}
