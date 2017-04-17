package edu.brown.cs.jmrs.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ServerCommandHandler implements Runnable {

  ServerWorker server;
  WebSocket    conn;
  String       unformattedCommand;

  public ServerCommandHandler(
      ServerWorker server,
      WebSocket conn,
      String command) {
    this.server = server;
    this.conn = conn;
    this.unformattedCommand = command;
  }

  @Override
  public void run() {
    Gson g = new Gson();
    JsonObject root = g.fromJson(unformattedCommand, JsonObject.class);

    Map<String, ?> commandMap = formMap(root);

    Player player = server.getPlayer(conn);

    if (commandMap.containsKey("command")) {

      if (((String) commandMap.get("command")).equals("set_player_id")) {
        String playerId = (String) commandMap.get("player_id");
        if (server.setPlayerId(conn, playerId)) {
          conn.send("SUCCESS");// TODO: json this
        } else {
          conn.send("ERROR: ID taken");// TODO: json this
        }
      } else if (player.getId().length() > 0) {
        // do server commands here
        switch ((String) commandMap.get("command")) {
          case "start_lobby":
            return;
          case "leave_lobby":
            return;
          case "join_lobby":
            return;
        }

        // if not a server command pass it to the lobby
        server.bundleMessageForLobby(conn).interpret(commandMap);
      } else {
        conn.send("ERROR: player must have id to create a lobby");// TODO: json
                                                                  // this
      }
    } else {
      conn.send("ERROR: invalid message from client");// TODO: json this
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
