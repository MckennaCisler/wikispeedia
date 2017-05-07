package edu.brown.cs.jmrs.server;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;

class ServerCommandHandler implements Runnable {

  private final ServerWorker       server;
  private final Session            conn;
  private final String             unformattedCommand;
  private final CommandInterpreter interpreter;
  private final Gson               gson;

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

  public ServerCommandHandler(
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

                if (commandPayload.has("arguments")) {
                  lobby.init(commandPayload.get("arguments").getAsJsonObject());
                }
                lobby.addClient(player.getId());
                player.setLobby(lobby);
                server.lobbylessMap().remove(player.getId());
                server.updateLobbylessPlayers();
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
                player.getLobby().removeClient(player.getId());
                player.setLobby(null);
                server.lobbylessMap().put(player.getId(), player);
                server.updateLobbylessPlayers();
                jsonObject.addProperty("error_message", "");
                toClient = gson.toJson(jsonObject);
                server.sendToClient(conn, toClient);
                return;

              case JOIN_LOBBY:
                jsonObject.addProperty("command", "join_lobby_response");
                if (!commandPayload.has("lobby_id")) {
                  throw new InputError("No lobby ID provided");
                }
                String lobbyId2 = commandPayload.get("lobby_id").getAsString();
                Lobby lobby2 = server.getLobby(lobbyId2);
                if (lobby2 == null) {
                  throw new InputError("No lobby with specified ID exists");
                }
                Lobby playerLobby = player.getLobby();
                if (playerLobby != null) {
                  playerLobby.removeClient(player.getId());
                }
                lobby2.addClient(player.getId());
                player.setLobby(lobby2);
                server.lobbylessMap().remove(player.getId());
                server.updateLobbylessPlayers();
                jsonObject.addProperty("error_message", "");
                toClient = gson.toJson(jsonObject);
                server.sendToClient(conn, toClient);
                return;
              default: // equivalent to NULL, i.e. that the command type was
                       // not
                       // in the Commands enum

                if (player.getLobby() != null) {
                  // if not a server command pass it to the lobby
                  interpreter
                      .interpret(player.getLobby(), player.getId(), commandMap);
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

  /**
   * Checks that a returned JSON commandMap follows the correct protocol.
   */
  private boolean commandMapStructure(JsonObject commandMap) {
    return commandMap.has("command") && commandMap.has("payload");
  }
}
