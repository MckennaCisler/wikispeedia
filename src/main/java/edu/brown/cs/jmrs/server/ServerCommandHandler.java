package edu.brown.cs.jmrs.server;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.server.errorhandling.ServerError;
import edu.brown.cs.jmrs.server.errorhandling.StaticErrorHandler;

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

  private void joinLobby(
      JsonObject jsonObject,
      JsonObject commandPayload,
      Client client) throws ServerError {
    jsonObject.addProperty("command", "join_lobby_response");
    StaticErrorHandler
        ._assert(commandPayload.has("lobby_id"), "No lobby ID provided.");

    String lobbyId2 = commandPayload.get("lobby_id").getAsString();
    Lobby lobby2 = server.getLobby(lobbyId2);
    StaticErrorHandler
        ._assert(lobby2 != null, "No lobby with specified ID exists.");

    synchronized (lobby2) {
      Lobby playerLobby = client.getLobby();
      if (playerLobby != null) {
        playerLobby.removeClient(client.getId());
      }
      lobby2.addClient(client.getId());
      client.setLobby(lobby2);
      server.lobbylessMap().remove(client.getId());
      server.updateLobbylessClients();
    }
    jsonObject.addProperty("error_message", "");
    server.sendToClient(conn, gson.toJson(jsonObject));
  }

  private void leaveLobby(JsonObject jsonObject, Client client)
      throws ServerError {
    jsonObject.addProperty("command", "leave_lobby_response");
    StaticErrorHandler._assert(
        client.getLobby() != null,
        "This client is not registered with any lobby.");

    synchronized (client.getLobby()) {
      client.getLobby().removeClient(client.getId());
      client.setLobby(null);
      server.lobbylessMap().put(client.getId(), client);
      server.updateLobbylessClients();
    }
    jsonObject.addProperty("error_message", "");
    server.sendToClient(conn, gson.toJson(jsonObject));
  }

  private void passToLobby(JsonObject commandMap, Client client)
      throws ServerError {
    StaticErrorHandler
        ._assert(client.getLobby() != null, "Player must join a lobby first.");

    // if not a server command pass it to the lobby
    interpreter.interpret(client.getLobby(), client.getId(), commandMap);
  }

  @Override
  public void run() {
    try {
      Gson g = new Gson();
      JsonObject commandMap = g.fromJson(unformattedCommand, JsonObject.class);

      StaticErrorHandler._assert(
          commandMapStructure(commandMap),
          "Commands must be JSON with 'command' and 'payload' fields.");
      StaticErrorHandler._assert(
          server.getClient(conn) != null,
          "No client object is registered for this Websocket connection.");

      String commandString = commandMap.get("command").getAsString();
      JsonObject commandPayload = commandMap.get("payload").getAsJsonObject();

      // objects for return message
      JsonObject jsonObject = new JsonObject();

      StaticErrorHandler._assert(
          server.getClient(conn).getId().length() > 0,
          "Client has no registered ID.");

      Client client = server.getClient(conn);
      synchronized (client) {
        // do server commands here
        try {
          switch (Commands.safeValueOf(commandString.toUpperCase())) {
            case START_LOBBY:
              startLobby(jsonObject, commandPayload, client);
              return;

            case LEAVE_LOBBY:
              leaveLobby(jsonObject, client);
              return;

            case JOIN_LOBBY:
              joinLobby(jsonObject, commandPayload, client);
              return;
            default: // equivalent to NULL, i.e. that the command type was
                     // not in the Commands enum
              passToLobby(commandMap, client);
              return;
          }
        } catch (ServerError e) {
          jsonObject.addProperty("error_message", e.getMessage());
          server.sendToClient(conn, gson.toJson(jsonObject));
        }
      }
    } catch (Throwable e) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("error_message", e.getMessage());
      server.sendToClient(conn, gson.toJson(jsonObject));
    }
  }

  private void startLobby(
      JsonObject jsonObject,
      JsonObject commandPayload,
      Client client) throws ServerError {
    jsonObject.addProperty("command", "start_lobby_response");
    StaticErrorHandler
        ._assert(commandPayload.has("lobby_id"), "No lobby ID provided");

    String lobbyId = commandPayload.get("lobby_id").getAsString();
    JsonObject args = null;
    if (commandPayload.has("arguments")) {
      args = commandPayload.get("arguments").getAsJsonObject();
    }
    server.createLobby(lobbyId, client, args);
    jsonObject.addProperty("error_message", "");
    server.sendToClient(conn, gson.toJson(jsonObject));
  }
}
