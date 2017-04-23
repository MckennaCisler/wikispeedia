package edu.brown.cs.jmrs.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;

class ServerCommandHandler implements Runnable {

  ServerWorker       server;
  WebSocket          conn;
  String             unformattedCommand;
  CommandInterpreter interpreter;

  private enum Commands {
    SET_CLIENT_ID,
    START_LOBBY,
    LEAVE_LOBBY,
    JOIN_LOBBY,
    GET_LOBBIES,
    // Value to signify not in enum (when using switch statement)
    NULL;

    /**
     * @return Whether the given commandName matches (is) this Command.
     */
    boolean is(String commandName) {
      try {
        return this.equals(valueOf(commandName.toUpperCase()));
      } catch (IllegalArgumentException | NullPointerException e) {
        return false;
      }
    }

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
      WebSocket conn,
      String command,
      CommandInterpreter interpreter) {
    this.server = server;
    this.conn = conn;
    this.unformattedCommand = command;
    this.interpreter = interpreter;
  }

  @Override
  public void run() {
    Gson g = new Gson();
    JsonObject commandMap = g.fromJson(unformattedCommand, JsonObject.class);
    assert commandMapStructure(commandMap);
    String commandString = commandMap.get("command").getAsString();
    JsonObject commandPayload = commandMap.get("payload").getAsJsonObject();

    // objects for return message
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    String toClient;

    if (commandMap.has("command")) {
      if (Commands.SET_CLIENT_ID.is(commandString)) {
        jsonObject.addProperty("command", "set_id_response");
        String clientId = commandPayload.get("client_id").getAsString();
        try {
          String trueId = server.setPlayerId(conn, clientId);

          jsonObject.addProperty("client_id", trueId);
          jsonObject.addProperty("error_message", "");
          toClient = gson.toJson(jsonObject);
          conn.send(toClient);

        } catch (InputError e) {
          jsonObject.addProperty("client_id", server.getPlayer(conn).getId());
          jsonObject.addProperty("error_message", e.getMessage());
          toClient = gson.toJson(jsonObject);
          conn.send(toClient);

        }
      } else if (server.getPlayer(conn).getId().length() > 0) {
        Player player = server.getPlayer(conn);
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

              if (commandMap.has("arguments")) {
                lobby.init(commandPayload.get("arguments").getAsJsonObject());
              }
              lobby.addClient(player.getId());
              player.setLobby(lobby);
              jsonObject.addProperty("error_message", "");
              toClient = gson.toJson(jsonObject);
              conn.send(toClient);
              return;

            case LEAVE_LOBBY:
              jsonObject.addProperty("command", "leave_lobby_response");
              if (player.getLobby() == null) {
                throw new InputError(
                    "This client is not registered with any lobby");
              }
              player.getLobby().removeClient(player.getId());
              jsonObject.addProperty("error_message", "");
              toClient = gson.toJson(jsonObject);
              conn.send(toClient);
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
              jsonObject.addProperty("error_message", "");
              toClient = gson.toJson(jsonObject);
              conn.send(toClient);
              return;
            case GET_LOBBIES:
              jsonObject.addProperty("command", "get_lobbies_response");
              List<String> lobbies = server.getOpenLobbies();
              jsonObject.addProperty("error_message", "");

              JsonArray lobbyArray = new JsonArray();
              for (String id : lobbies) {
                lobbyArray.add(id);
              }

              jsonObject.add("lobbies", lobbyArray);
              return;
            default: // equivalent to NULL, i.e. that the command type was not
                     // in the Commands enum

              if (player.getLobby() != null) {
                // if not a server command pass it to the lobby
                interpreter
                    .interpret(player.getLobby(), player.getId(), commandMap);
              } else {
                jsonObject.addProperty(
                    "error_message",
                    "Player must join a lobby first.");
                toClient = gson.toJson(jsonObject);
                conn.send(toClient);
              }
              return;
          }
        } catch (InputError e) {
          jsonObject.addProperty("error_message", e.getMessage());
          toClient = gson.toJson(jsonObject);
          conn.send(toClient);
        }
      } else {
        jsonObject
            .addProperty("error_message", "Cannot continue without unique ID");
        toClient = gson.toJson(jsonObject);
        conn.send(toClient);
      }
    } else {
      jsonObject.addProperty("command", "command_error");
      jsonObject.addProperty(
          "error_message",
          "Client-to-Server commands must be JSON with 'command' field");
      toClient = gson.toJson(jsonObject);
      conn.send(toClient);
    }
  }

  /**
   * Checks that a returned JSON commandMap follows the correct protocol.
   */
  private boolean commandMapStructure(JsonObject commandMap) {
    return commandMap.has("command") && commandMap.has("payload");
  }

  @Deprecated
  private Map<String, ?> formMap(JsonObject json) {
    Map<String, Object> argMap = new HashMap<>();

    Set<Entry<String, JsonElement>> elements = json.entrySet();
    for (Entry<String, JsonElement> entry : elements) {
      String fieldName = entry.getKey();
      JsonElement rawValue = entry.getValue();
      if (rawValue.isJsonObject()) {
        argMap.put(fieldName, formMap(rawValue.getAsJsonObject()));
      } else {
        argMap.put(fieldName, rawValue.getAsString());
      }
    }

    return argMap;
  }
}
