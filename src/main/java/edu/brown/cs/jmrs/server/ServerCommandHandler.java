package edu.brown.cs.jmrs.server;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;

/**
 * Command handler for the built-in commands the server architecture requires.
 *
 * @author shastin1
 *
 */
class ServerCommandHandler implements Runnable {

  /**
   * All server commands.
   *
   * @author shastin1
   *
   */
  private enum Commands {
    SET_CLIENT_ID,
    START_LOBBY,
    LEAVE_LOBBY,
    JOIN_LOBBY,
    GET_LOBBIES,
    // Value to signify not in enum (when using switch statement)
    NULL;

    /**
     * Equivalent to valueOf() but returns a blank string instead of an
     * exception.
     */
    static Commands safeValueOf(String commandName) {
      try {
        return valueOf(commandName);
      } catch (IllegalArgumentException | NullPointerException e) {
        return NULL;
      }
    }
  }

  private final ServerWorker       server;
  private final Session            conn;
  private final String             unformattedCommand;
  private final CommandInterpreter interpreter;

  private final Gson               gson;

  /**
   * Constructor, stores all necessary objects for analyzing and acting on
   * commands when a thread is available.
   *
   * @param server
   *          The server instance to act on
   * @param conn
   *          The connection to the client that sent the command
   * @param command
   *          The command (as stringified JSON) sent by the client
   * @param interpreter
   *          The command interpreter to forward the command to if it is not a
   *          built-in command
   * @param gson
   *          The Gson instance used for JSONification of lobbies
   */
  ServerCommandHandler(
      ServerWorker server,
      Session conn,
      String command,
      CommandInterpreter interpreter,
      Gson gson) {
    this.server = server;
    this.conn = conn;
    this.unformattedCommand = command;
    this.interpreter = interpreter;
    this.gson = gson;
  }

  /**
   * Checks that a returned JSON commandMap follows the correct protocol.
   *
   * @param commandMap
   *          The command to check the structure of
   * @return whether the command abides by the set structure
   */
  private boolean commandMapStructure(JsonObject commandMap) {
    return commandMap.has("command") && commandMap.has("payload");
  }

  @Override
  public void run() {
    try {
      Gson g = new Gson();
      JsonObject commandMap = g.fromJson(unformattedCommand, JsonObject.class);
      assert commandMapStructure(commandMap);
      String commandString = commandMap.get("command").getAsString();
      JsonObject commandPayload = commandMap.get("payload").getAsJsonObject();

      // objects for return message
      JsonObject jsonObject = new JsonObject();
      String toClient;

      if (commandMap.has("command")) {
        assert server.getClient(conn) != null;

        if (server.getClient(conn).getId().length() > 0) {
          Client player = server.getClient(conn);
          synchronized (player) {
            // do server commands here
            try {
              switch (Commands.safeValueOf(commandString.toUpperCase())) {
                case START_LOBBY:
                  jsonObject.addProperty("command", "start_lobby_response");
                  if (!commandPayload.has("lobby_id")) {
                    throw new InputError("No lobby ID provided");
                  }
                  String lobbyId = commandPayload.get("lobby_id").getAsString();
                  Lobby lobby = server.createLobby(lobbyId);

                  // note lobby will not be null
                  synchronized (lobby) {
                    if (commandPayload.has("arguments")) {
                      lobby.init(
                          commandPayload.get("arguments").getAsJsonObject());
                    }
                    lobby.addClient(player.getId());
                    player.setLobby(lobby);
                    server.lobbylessMap().remove(player.getId());
                    server.updateLobbylessClients();
                  }
                  jsonObject.addProperty("error_message", "");
                  toClient = gson.toJson(jsonObject);
                  server.sendToClient(conn, toClient);
                  return;

                case LEAVE_LOBBY:
                  jsonObject.addProperty("command", "leave_lobby_response");
                  if (player.getLobby() == null) {
                    throw new InputError(
                        "This client is not registered with any lobby");
                  }
                  synchronized (player.getLobby()) {
                    player.getLobby().removeClient(player.getId());
                    player.setLobby(null);
                    server.lobbylessMap().put(player.getId(), player);
                    server.updateLobbylessClients();
                  }
                  jsonObject.addProperty("error_message", "");
                  toClient = gson.toJson(jsonObject);
                  server.sendToClient(conn, toClient);
                  return;

                case JOIN_LOBBY:
                  jsonObject.addProperty("command", "join_lobby_response");
                  if (!commandPayload.has("lobby_id")) {
                    throw new InputError("No lobby ID provided");
                  }
                  String lobbyId2 = commandPayload.get("lobby_id")
                      .getAsString();
                  Lobby lobby2 = server.getLobby(lobbyId2);
                  if (lobby2 == null) {
                    throw new InputError("No lobby with specified ID exists");
                  }
                  synchronized (lobby2) {
                    Lobby playerLobby = player.getLobby();
                    if (playerLobby != null) {
                      playerLobby.removeClient(player.getId());
                    }
                    lobby2.addClient(player.getId());
                    player.setLobby(lobby2);
                    server.lobbylessMap().remove(player.getId());
                    server.updateLobbylessClients();
                  }
                  jsonObject.addProperty("error_message", "");
                  toClient = gson.toJson(jsonObject);
                  server.sendToClient(conn, toClient);
                  return;
                default: // equivalent to NULL, i.e. that the command type was
                         // not in the Commands enum

                  if (player.getLobby() != null) {
                    // if not a server command pass it to the lobby
                    interpreter.interpret(
                        player.getLobby(),
                        player.getId(),
                        commandMap);
                  } else {
                    jsonObject.addProperty(
                        "error_message",
                        "Player must join a lobby first");
                    toClient = gson.toJson(jsonObject);
                    server.sendToClient(conn, toClient);
                  }
                  return;
              }
            } catch (InputError e) {
              jsonObject.addProperty("error_message", e.getMessage());
              toClient = gson.toJson(jsonObject);
              server.sendToClient(conn, toClient);
            }
          }
        } else {
          jsonObject.addProperty(
              "error_message",
              "Cannot continue without unique ID");
          toClient = gson.toJson(jsonObject);
          server.sendToClient(conn, toClient);
        }
      } else {
        jsonObject.addProperty("command", "command_error");
        jsonObject.addProperty(
            "error_message",
            "Client-to-Server commands must be JSON with 'command' field");
        toClient = gson.toJson(jsonObject);
        server.sendToClient(conn, toClient);
      }
    } catch (Throwable e) {
      // solely for debugging purposes, as threads do not display exceptions
      // except when calling the value of an associated future:
      // http://stackoverflow.com/questions/2248131/
      // handling-exceptions-from-java-executorservice-tasks
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("error_message", e.getMessage());
      String toClient = gson.toJson(jsonObject);
      server.sendToClient(conn, toClient);
    }
  }
}
