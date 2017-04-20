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
    SET_CLIENT_ID, START_LOBBY, LEAVE_LOBBY, JOIN_LOBBY, GET_LOBBIES
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
    JsonObject root = g.fromJson(unformattedCommand, JsonObject.class);

    Map<String, ?> commandMap = formMap(root);

    Player player = server.getPlayer(conn);

    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();

    String commandString = (String) commandMap.get("command");

    if (commandMap.containsKey("command")) {
      if (Commands.valueOf(commandString) == Commands.SET_CLIENT_ID) {
        jsonObject.addProperty("type", "set_id_reponse");
        String clientId = (String) commandMap.get("client_id");
        try {
          server.setPlayerId(conn, clientId);
          String trueId = clientId.length() == 0
              ? server.getPlayer(conn).getId() : clientId;
          jsonObject.addProperty("client_id", trueId);
          jsonObject.addProperty("error_message", "");
          String toClient = gson.toJson(jsonObject);
          conn.send(toClient);
        } catch (InputError e) {
          jsonObject.addProperty("client_id", player.getId());
          jsonObject.addProperty("error_message", e.getMessage());
          String toClient = gson.toJson(jsonObject);
          conn.send(toClient);
        }
      } else if (player.getId().length() > 0) {
        // do server commands here
        try {
          switch (Commands.valueOf(commandString.toUpperCase())) {
            case START_LOBBY: {
              jsonObject.addProperty("type", "start_lobby_reponse");
              if (!commandMap.containsKey("lobby_id")) {
                throw new InputError("No lobby ID provided");
              }
              String lobbyId = (String) commandMap.get("lobby_id");
              Lobby lobby = server.createLobby(lobbyId);

              if (commandMap.containsKey("arguments")) {
                lobby.init((Map<String, ?>) commandMap.get("arguments"));
              }
              lobby.addClient(player.getId());
              player.setLobby(lobby);
              jsonObject.addProperty("error_message", "");
              String toClient = gson.toJson(jsonObject);
              conn.send(toClient);
              return;
            }
            case LEAVE_LOBBY: {
              jsonObject.addProperty("type", "leave_lobby_reponse");
              if (player.getLobby() == null) {
                throw new InputError(
                    "This client is not registered with any lobby");
              }
              player.getLobby().removeClient(player.getId());
              jsonObject.addProperty("error_message", "");
              String toClient = gson.toJson(jsonObject);
              conn.send(toClient);
              return;
            }
            case JOIN_LOBBY:
              jsonObject.addProperty("type", "join_lobby_reponse");
              if (!commandMap.containsKey("lobby_id")) {
                throw new InputError("No lobby ID provided");
              }
              String lobbyId = (String) commandMap.get("lobby_id");
              Lobby lobby = server.getLobby(lobbyId);
              if (lobby == null) {
                throw new InputError("No lobby with specified ID exists");
              }
              Lobby playerLobby = player.getLobby();
              if (playerLobby != null) {
                playerLobby.removeClient(player.getId());
              }
              lobby.addClient(player.getId());
              player.setLobby(lobby);
              jsonObject.addProperty("error_message", "");
              String toClient = gson.toJson(jsonObject);
              conn.send(toClient);
              return;
            case GET_LOBBIES:
              jsonObject.addProperty("type", "get_lobbies_reponse");
              List<String> lobbies = server.getOpenLobbies();
              jsonObject.addProperty("error_message", "");

              JsonArray lobbyArray = new JsonArray();
              for (String id : lobbies) {
                lobbyArray.add(id);
              }

              jsonObject.add("lobbies", lobbyArray);
              return;
            default:
              System.out.println("Find Sean and tell him he broke this.");
              break;
          }
        } catch (InputError e) {
          jsonObject.addProperty("error_message", e.getMessage());
          String toClient = gson.toJson(jsonObject);
          conn.send(toClient);
        }

        // if not a server command pass it to the lobby
        interpreter.interpret(player.getLobby(), player.getId(), commandMap);
      } else {
        jsonObject
            .addProperty("error_message", "Cannot continue without unique ID");
        String toClient = gson.toJson(jsonObject);
        conn.send(toClient);
      }
    } else {
      jsonObject.addProperty("type", "command_error");
      jsonObject.addProperty(
          "error_message",
          "Client-to-Server commands must be JSON with 'command' field");
      String toClient = gson.toJson(jsonObject);
      conn.send(toClient);
    }
  }

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
