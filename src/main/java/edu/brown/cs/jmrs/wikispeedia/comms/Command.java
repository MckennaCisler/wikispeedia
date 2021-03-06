package edu.brown.cs.jmrs.wikispeedia.comms;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.wikispeedia.WikiLobby;
import edu.brown.cs.jmrs.wikispeedia.WikiPlayer;

/**
 * All possible incoming and outgoing commands for the WikiInterpreter. Also
 * used in WikiLobby for OUTGOING commands.
 *
 * @author mcisler
 *
 */
public enum Command {
  /**
   * INCOMING Commands.
   */
  // Lobby-specific commands
  GET_PLAYERS("get_players", CommandType.INCOMING, "lobby_id"), //
  GET_TIME("get_time", CommandType.INCOMING, "lobby_id"), //
  GET_SETTINGS("get_settings", CommandType.INCOMING, "lobby_id", "state"), //
  FORCE_BEGIN_GAME("force_begin_game", CommandType.INCOMING), //
  GET_PAGE("get_page", CommandType.INCOMING, "page_name"), //
  SEND_MESSAGE("send_message", CommandType.INCOMING, "message"), //
  GET_MESSAGES("get_messages", CommandType.INCOMING/* , "date_since" */), //
  // Player-specific commands
  SET_USERNAME("set_username", CommandType.INCOMING, "username"), //
  SET_PLAYER_STATE("set_player_state", CommandType.INCOMING, "state"), //
  GOTO_PAGE("goto_page", CommandType.INCOMING, "page_name"), //
  GO_BACK_PAGE("go_back_page", CommandType.INCOMING), //
  GET_PATH("get_path", CommandType.INCOMING), //
  // GET_LINKS("get_links", CommandType.INCOMING);
  /**
   * RESPONSEs to INCOMING Commands.
   */
  // Lobby-specific commands
  RETURN_TIME("return_time", CommandType.RESPONSE), //
  RETURN_SETTINGS("return_settings", CommandType.RESPONSE), //
  RETURN_GET_PAGE("return_get_page", CommandType.RESPONSE), //
  RETURN_SET_USERNAME("return_set_username", CommandType.RESPONSE), //
  RETURN_GOTO_PAGE("return_goto_page", CommandType.RESPONSE), //
  RETURN_PATH("return_path", CommandType.RESPONSE), //
  RETURN_MESSAGES("return_messages", CommandType.RESPONSE), //

  /**
   * OUTGOING Server Commands (see below for constructions).
   */
  END_GAME("end_game", CommandType.OUTGOING), //
  BEGIN_GAME("begin_game", CommandType.OUTGOING), //
  ALL_PLAYERS("all_players", CommandType.OUTGOING), //
  ERROR("error", CommandType.OUTGOING), //

  // Value to signify not in enum (when using switch statement)
  NULL(null, null);

  /**
   * The type of commands in this enum.
   */
  enum CommandType {
    INCOMING, // incoming commands for RESPONSEs
    RESPONSE, // responses to INCOMING commands
    OUTGOING; // server event-based commands
  }

  /**
   * A specific enum for game states.
   */
  enum GameState {
    WAITING, STARTED, ENDED
  }

  private final String      command;
  private final CommandType type;
  private String[]          args;

  /**
   * Creates a particular defined command based on command.
   *
   * @param command
   *          The command to create.
   */
  Command(String command, CommandType type, String... args) {
    this.command = command;
    this.type = type;
    this.args = args;
  }

  /**
   * @return The raw command name.
   */
  public String command() {
    return command;
  }

  /**
   * @return Arguments to this command.
   */
  public String[] args() {
    return args;
  }

  /**
   * Builds the given command from the provided arguments.
   *
   * @param data
   *          An object to use GSON to convert into JSON.
   * @return A JSON string ready for sending.
   */
  public String build(Object data) {
    return build(data, "");
  }

  /**
   * Builds the given command from the provided arguments.
   *
   * @param data
   *          An object to use GSON to convert into JSON.
   * @param errorMessage
   *          An optional error message to display.
   * @return A JSON string ready for sending.
   */
  private String build(Object data, String errorMessage) {
    assert type.equals(CommandType.RESPONSE)
        || type.equals(CommandType.OUTGOING);
    JsonObject root = new JsonObject();
    root.addProperty("command", command);
    root.addProperty("error_message", errorMessage);
    root.add("payload", Main.GSON.toJsonTree(data));
    return Main.GSON.toJson(root);
  }

  /**
   * Equivalent to valueOf() but returns a blank string instead of an exception.
   */
  static Command safeValueOf(String commandName) {
    try {
      return valueOf(commandName);
    } catch (IllegalArgumentException | NullPointerException e) {
      return NULL;
    }
  }

  /**
   * Sends a JSONified version of data to the client with clientId.
   *
   * @param server
   *          The server to use to send.
   * @param clientId
   *          The clientId.
   * @param data
   *          The data to JSONify and send.
   */
  public void send(Server server, String clientId, Object data) {
    send(server, clientId, data, "");
  }

  /**
   * Sends a JSONified version of data to the client with clientId.
   *
   * @param server
   *          The server to use to send.
   * @param clientId
   *          The clientId.
   * @param data
   *          The data to JSONify and send.
   * @param errorMessage
   *          An error message to send as well.
   */
  public void send(Server server, String clientId, Object data,
      String errorMessage) {
    server.sendToClient(clientId, build(data, errorMessage));
  }

  /**
   * Sends an error message with a generic message.
   *
   * @param server
   *          The server to use to send.
   * @param clientId
   *          The clientId.
   * @param errorMessage
   *          The error message.
   */
  public static void sendError(Server server, String clientId,
      String errorMessage) {
    Main.debugLog("Command.ERROR sent: " + errorMessage);
    ERROR.send(server, clientId, ImmutableMap.of(), errorMessage);
  }

  /**
   * Sends a to all players in lobby a JSONified version of data.
   *
   * @param lobby
   *          The lobby to send to get players from.
   * @param data
   *          The data to send.
   */
  public void sendToAll(WikiLobby lobby, Object data) {
    sendToAll(lobby, data, "");
  }

  /**
   * Sends a to all players in lobby a JSONified version of data, and an error
   * message.
   *
   * @param lobby
   *          The lobby to send to get players from.
   * @param data
   *          The data to send.
   * @param errorMessage
   *          An error message to send as well.
   */
  public void sendToAll(WikiLobby lobby, Object data, String errorMessage) {
    for (WikiPlayer player : lobby.getConnectedPlayers()) {
      send(lobby.getServer(), player.getId(), data, errorMessage);
    }
  }

  /*
   * Java functions to actually send Commands.
   */
  /**
   * @param lobby
   *          The lobby to get players from.
   */
  public static void sendAllPlayers(WikiLobby lobby) {
    Main.debugLog(String.format("All players in lobby '%s': %s", lobby,
        lobby.getConnectedPlayers()));
    Command.ALL_PLAYERS.sendToAll(lobby, lobby.getConnectedPlayers());
  }

  /**
   * @param lobby
   *          The lobby to get players and winner from.
   */
  public static void sendEndGame(WikiLobby lobby) {
    Command.END_GAME.sendToAll(lobby, lobby.getWinners());
  }

  /**
   * @param lobby
   *          The lobby to get players from.
   */
  public static void sendBeginGame(WikiLobby lobby) {
    sendBeginGame(lobby, "");
  }

  /**
   * @param lobby
   *          The lobby to get players and winner from.
   * @param error
   *          The error message to all.
   */
  public static void sendBeginGame(WikiLobby lobby, String error) {
    Command.BEGIN_GAME.sendToAll(lobby, ImmutableMap.of(), error);
  }
}
