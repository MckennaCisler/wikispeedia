package edu.brown.cs.jmrs.server.example.chatroom;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.server.customizable.Lobby;

public class ChatLobby implements Lobby {

  private String id;
  private List<String> connectedPlayers;
  private List<String> disconnectedPlayers;
  private Server server;
  private boolean closed;

  public ChatLobby(Server server, String id) {
    this.server = server;
    this.id = id;
    this.connectedPlayers = new CopyOnWriteArrayList<>();
    this.disconnectedPlayers = new CopyOnWriteArrayList<>();
    this.closed = false;
  }

  public void close() {
    server.closeLobby(id);
    closed = true;
    id = null;
    disconnectedPlayers = null;
    connectedPlayers = null;
    server = null;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public void addClient(String playerId) {
    connectedPlayers.add(playerId);
  }

  public void sendMessage(String fromId, String message) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("command", "message");
    jsonObject.addProperty("error_message", "");
    jsonObject.addProperty("sender", fromId);
    jsonObject.addProperty("message", message);

    String toClient = gson.toJson(jsonObject);

    for (String playerId : connectedPlayers) {
      if (!playerId.equals(fromId)) {
        server.sendToClient(playerId, toClient);
      }
    }
  }

  public void sendMessage(String recipientId, String fromId, String message) {
    Gson gson = new Gson();

    for (String playerId : connectedPlayers) {
      if (playerId.equals(recipientId)) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("command", "whisper");
        jsonObject.addProperty("error_message", "");
        jsonObject.addProperty("sender", fromId);
        jsonObject.addProperty("message", message);

        String toClient = gson.toJson(jsonObject);
        server.sendToClient(playerId, toClient);
        return;
      }
    }

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("command", "bounced_whisper");
    jsonObject.addProperty("error_message",
        "No client with specified ID exists in this lobby");
    jsonObject.addProperty("target", recipientId);

    String toClient = gson.toJson(jsonObject);
    server.sendToClient(fromId, toClient);
  }

  @Override
  public void removeClient(String playerId) {
    connectedPlayers.remove(playerId);
  }

  @Override
  public void init(JsonObject arguments) {
  }

  @Override
  public void playerReconnected(String clientId) {
    if (disconnectedPlayers.contains(clientId)) {
      connectedPlayers.add(clientId);
      disconnectedPlayers.remove(clientId);
    }
  }

  @Override
  public void playerDisconnected(String clientId) {
    if (connectedPlayers.contains(clientId)) {
      disconnectedPlayers.add(clientId);
      connectedPlayers.remove(clientId);
    }
  }

   @Override
   public JsonElement toJson(Gson gson) {
   JsonObject obj = new JsonObject();
   obj.addProperty("id", id);
   return obj;
   }
}
