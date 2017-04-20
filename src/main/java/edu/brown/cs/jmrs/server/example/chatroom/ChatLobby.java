package edu.brown.cs.jmrs.server.example.chatroom;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.server.customizable.Lobby;

public class ChatLobby implements Lobby {

  private String       id;
  private List<String> playerIds;
  private Server       server;
  private boolean      closed;

  public ChatLobby() {
  }

  public ChatLobby(Server server, String id) {
    this.server = server;
    this.id = id;
    this.playerIds = new CopyOnWriteArrayList<>();
    this.closed = false;
  }

  public void close() {
    closed = true;
    id = null;
    playerIds = null;
    server = null;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public void addClient(String playerId) {
    playerIds.add(playerId);
  }

  public void sendMessage(String fromId, String message) {

    System.out.println(fromId + ": " + message);
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("type", "message");
    jsonObject.addProperty("error", "");
    jsonObject.addProperty("sender", fromId);
    jsonObject.addProperty("message", message);

    String toClient = gson.toJson(jsonObject);

    for (String playerId : playerIds) {
      if (!playerId.equals(fromId)) {
        server.sendToClient(playerId, toClient);
      }
    }
  }

  public void sendMessage(String recipientId, String fromId, String message) {
    Gson gson = new Gson();

    for (String playerId : playerIds) {
      if (playerId.equals(recipientId)) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "whisper");
        jsonObject.addProperty("error", "");
        jsonObject.addProperty("sender", fromId);
        jsonObject.addProperty("message", message);

        String toClient = gson.toJson(jsonObject);
        server.sendToClient(playerId, toClient);
        return;
      }
    }

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("type", "bounced_whisper");
    jsonObject.addProperty(
        "error",
        "No client with specified ID exists in this lobby");
    jsonObject.addProperty("target", recipientId);

    String toClient = gson.toJson(jsonObject);
    server.sendToClient(fromId, toClient);
  }

  @Override
  public void removeClient(String playerId) {
    playerIds.remove(playerId);
  }

  @Override
  public void init(Map<String, ?> arguments) {
  }
}
