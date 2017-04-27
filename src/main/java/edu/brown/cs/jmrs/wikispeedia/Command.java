package edu.brown.cs.jmrs.wikispeedia;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.Server;

/**
 * All possible incoming and outgoing commands for the WikiInterpreter. Also
 * used in WikiLobby for OUTGOING commands.
 *
 * @author mcisler
 *
 */
enum Command {
  /**
   * INCOMING Commands.
   */
  // Lobby-specific commands
  GET_PLAYERS("get_players", CommandType.INCOMING, "lobby_id"), //
  GET_TIME("get_time", CommandType.INCOMING, "lobby_id"), //
  GET_SETTINGS("get_settings", CommandType.INCOMING, "lobby_id", "state"), //
  FORCE_BEGIN_GAME("force_begin_game", CommandType.INCOMING), //
  GET_PAGE("get_page", CommandType.INCOMING, "page_name"), //
  // Player-specific commands
  SET_USERNAME("set_username", CommandType.INCOMING, "username"), //
  SET_PLAYER_STATE("set_player_state", CommandType.INCOMING, "state"), //
  GOTO_PAGE("goto_page", CommandType.INCOMING, "page_name"), //
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
  ERROR("error", CommandType.RESPONSE), //

  /**
   * OUTGOING Server Commands (see below for constructions).
   */
  END_GAME("end_game", CommandType.OUTGOING), //
  BEGIN_GAME("begin_game", CommandType.OUTGOING), //
  ALL_PLAYERS("all_players", CommandType.OUTGOING), //
  // ALL_LOBBIES("all_lobbies", CommandType.OUTGOING),

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

  private final String command;
  private final CommandType type;
  private String[] args;

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

  public String command() {
    return command;
  }

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
    root.add("payload", WikiInterpreter.GSON.toJsonTree(data));
    return WikiInterpreter.GSON.toJson(root);
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
   */
  void send(Server server, String clientId, Object data) {
    send(server, clientId, data, "");
  }

  /**
   * Sends a JSONified version of data to the client with clientId.
   */
  void send(Server server, String clientId, Object data, String errorMessage) {
    server.sendToClient(clientId, build(data));
  }

  /**
   * Sends a to all players in lobby a JSONified version of data.
   */
  void sendToAll(WikiLobby lobby, Object data) {
    sendToAll(lobby, data, "");
  }

  /**
   * Sends a to all players in lobby a JSONified version of data, and an error
   * message.
   */
  void sendToAll(WikiLobby lobby, Object data, String errorMessage) {
    for (WikiPlayer player : lobby.getPlayers()) {
      send(lobby.getServer(), player.getId(), data, errorMessage);
    }
  }

  /*
   * Java functions to actually send Commands.
   */
  static void sendAllPlayers(WikiLobby lobby) {
    Command.ALL_PLAYERS.sendToAll(lobby, lobby.getPlayers());
  }

  static void sendEndGame(WikiLobby lobby) {
    Command.END_GAME.sendToAll(lobby, lobby.getWinner());
  }

  static void sendBeginGame(WikiLobby lobby) {
    sendBeginGame(lobby, "");
  }

  static void sendBeginGame(WikiLobby lobby, String error) {
    Command.BEGIN_GAME.sendToAll(lobby, ImmutableMap.of(), error);
  }
}
