package edu.brown.cs.jmrs.server;

import java.io.IOException;
import java.net.HttpCookie;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiFunction;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.collect.ConcurrentBiMap;
import edu.brown.cs.jmrs.server.customizable.Lobby;

class ServerWorker {

  private Server                           server;
  private LobbyManager                     lobbies;
  private ConcurrentBiMap<Session, Client> clients;
  private Map<String, Client>              notInLobbies;
  private Queue<Client>                    disconnectedClients;
  private Gson                             gson;

  public ServerWorker(
      Server server,
      BiFunction<Server, String, ? extends Lobby> lobbyFactory,
      Gson gson) {
    this.server = server;
    lobbies = new LobbyManager(lobbyFactory);
    clients = new ConcurrentBiMap<>();
    notInLobbies = new ConcurrentHashMap<>();
    disconnectedClients = new PriorityBlockingQueue<>();
    this.gson = gson;
  }

  public String setClientId(Session conn, String clientId) throws InputError {
    Client client = clients.getBack(new Client(clientId));
    if (client == null) {
      clientId = conn.hashCode() + "";
      client = new Client(clientId);
      while (!clients.putNoOverwrite(conn, client)) {
        clientId = Math.random() + "";
        client = new Client(clientId);
      }
    } else if (!client.isConnected()) {
      clients.put(conn, client);
      client.toggleConnected();
      if (client.getLobby() != null) {
        client.getLobby().playerReconnected(client.getId());
      }
    } else {
      throw new InputError("Don't steal identities");
    }
    return client.getId();
  }

  public Client getClient(Session conn) {
    return clients.get(conn);
  }

  public Session getClient(String playerId) {
    return clients.getReversed(new Client(playerId));
  }

  public List<String> getOpenLobbies() {
    return lobbies.getOpenLobbies();
  }

  public Lobby createLobby(String lobbyId) throws InputError {
    Lobby lobby = lobbies.create(lobbyId, server);
    if (lobby == null) {
      throw new InputError("Lobby ID in use");
    } else {
      return lobby;
    }
  }

  public Lobby getLobby(String lobbyId) {
    return lobbies.get(lobbyId);
  }

  public void closeLobby(String lobbyId) {
    lobbies.remove(lobbyId);
  }

  public void playerDisconnected(Session conn) {
    Date expiration = new Date();
    List<HttpCookie> cookies = conn.getUpgradeRequest().getCookies();
    for (HttpCookie cookie : cookies) {
      if (cookie.getName().equals("client_id")) {
        String cookieVal = cookie.getValue();
        expiration = Date.from(
            Instant.ofEpochMilli(
                Long.parseLong(
                    cookieVal.substring(cookieVal.indexOf(":") + 1))));
        break;
      }
    }

    Client client = clients.get(conn);
    if (client != null && client.getLobby() == null) {
      notInLobbies.remove(client.getId());
    }

    if (expiration.after(new Date())) {
      if (client != null) {
        assert client.isConnected();
        client.toggleConnected();

        client.setCookieExpiration(expiration);
        disconnectedClients.add(client);

        if (client.getLobby() != null) {
          client.getLobby().playerDisconnected(client.getId());
        }
      }
    } else {
      clients.remove(conn);
    }
  }

  private void checkDisconnectedPlayers() {
    if (!disconnectedClients.isEmpty()) {
      Date now = new Date();
      Client p = disconnectedClients.poll();
      while (p.getCookieExpiration().before(now)) {
        clients.remove(clients.getReversed(p));
        if (disconnectedClients.isEmpty()) {
          return;
        }
        p = disconnectedClients.poll();
      }
      disconnectedClients.add(p);
    }
  }

  private JsonObject allLobbies() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("command", "all_lobbies");
    List<String> lobbies = getOpenLobbies();
    jsonObject.addProperty("error_message", "");

    JsonArray lobbyArray = new JsonArray();
    for (String id : lobbies) {
      lobbyArray.add(getLobby(id).toJson(gson));
    }

    jsonObject.add("payload", lobbyArray);
    return jsonObject;
  }

  public void playerConnected(Session conn) {
    checkDisconnectedPlayers();

    String clientId = "";
    List<HttpCookie> cookies = conn.getUpgradeRequest().getCookies();
    for (HttpCookie cookie : cookies) {
      if (cookie.getName().equals("client_id")) {
        String cookieString = cookie.getValue();

        clientId = cookieString.substring(0, cookieString.indexOf(":"));
        break;
      }
    }

    // notify client of their id

    String toClient = "";
    String trueId = "";

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("command", "notify_id");
    try {
      try {
        trueId = setClientId(conn, clientId);

        Client client = clients.get(conn);
        if (!client.isConnected()) {
          client.toggleConnected();
        }

        jsonObject.addProperty("client_id", trueId);
        jsonObject.addProperty("error_message", "");
        toClient = gson.toJson(jsonObject);
        conn.getRemote().sendString(toClient);

      } catch (InputError e) {
        jsonObject.addProperty("client_id", "");
        jsonObject.addProperty("error_message", e.getMessage());
        toClient = gson.toJson(jsonObject);
        conn.getRemote().sendString(toClient);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // if they are not in a lobby, give them a list of lobbies

    Client client = clients.get(conn);
    if (client != null && client.getLobby() == null) {
      notInLobbies.put(client.getId(), client);
    }
    sendLobbies(clients.get(trueId));
  }

  public void updateLobbylessPlayers() {
    for (Client client : notInLobbies.values()) {
      sendLobbies(client);
    }
  }

  private void sendLobbies(Client client) {
    JsonObject jsonObject = allLobbies();
    jsonObject.addProperty("command", "get_lobbies");
    jsonObject.add("lobbies", allLobbies());
    jsonObject.addProperty("error_message", "");
    String toClient = gson.toJson(jsonObject);
    try {
      clients.getReversed(client).getRemote().sendString(toClient);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Map<String, Client> lobbylessMap() {
    return notInLobbies;
  }
}
