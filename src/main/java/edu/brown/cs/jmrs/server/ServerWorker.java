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

  private Server server;
  private LobbyManager lobbies;
  private ConcurrentBiMap<Session, Player> players;
  private Map<String, Player> notInLobbies;
  private Queue<Player> disconnectedPlayers;
  private final Gson gson;

  public ServerWorker(Server server,
      BiFunction<Server, String, ? extends Lobby> lobbyFactory, Gson gson) {
    this.server = server;
    lobbies = new LobbyManager(lobbyFactory);
    players = new ConcurrentBiMap<>();
    notInLobbies = new ConcurrentHashMap<>();
    disconnectedPlayers = new PriorityBlockingQueue<>();
    this.gson = gson;
  }

  public String setPlayerId(Session conn, String playerId) throws InputError {
    Player player = players.getBack(new Player(playerId));
    if (player == null) {
      playerId = conn.hashCode() + "";
      player = new Player(playerId);
      while (!players.putNoOverwrite(conn, player)) {
        playerId = Math.random() + "";
        player = new Player(playerId);
      }

      players.put(conn, player); // TODO: make this not needed

    } else if (!player.isConnected()) {
      players.put(conn, player);
      player.toggleConnected();
      if (player.getLobby() != null) {
        player.getLobby().playerReconnected(player.getId());
      }
    } else {
      throw new InputError("Stop stealing identities");
    }
    return player.getId();
  }

  public Player getPlayer(Session conn) {
    return players.get(conn);
  }

  public Session getPlayer(String playerId) {
    return players.getReversed(new Player(playerId));
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
        expiration =
            Date.from(Instant.ofEpochMilli(Long
                .parseLong(cookieVal.substring(cookieVal.indexOf(":") + 1))));
        break;
      }
    }

    if (expiration.after(new Date())) {
      Player player = players.get(conn);
      assert player.isConnected();
      player.toggleConnected();

      player.setCookieExpiration(expiration);
      disconnectedPlayers.add(player);

      if (player.getLobby() != null) {
        player.getLobby().playerDisconnected(player.getId());
      }
    } else {
      players.remove(conn);
    }
  }

  private void checkDisconnectedPlayers() { 
    if (!disconnectedPlayers.isEmpty()) {
      Date now = new Date();
      Player p = disconnectedPlayers.poll();
      while (p != null && p.getCookieExpiration().before(now)) {
        players.remove(players.getReversed(p));
        if (!disconnectedPlayers.isEmpty()) {
          p = disconnectedPlayers.poll();
        } else {
          p = null;
        }
      }
      disconnectedPlayers.add(p);
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
        trueId = setPlayerId(conn, clientId);

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

    Player player = players.get(trueId);
	  if (player != null && player.getLobby() == null) {
		  notInLobbies.put(player.getId(), player);
		  
	   jsonObject = allLobbies();
	  jsonObject.addProperty("command", "get_lobbies");
	  jsonObject.addProperty("error_message", "");
	  toClient = gson.toJson(jsonObject);
	  try {
	  conn.getRemote().sendString(toClient);
	  } catch (IOException e) {
	    e.printStackTrace();
	  }
	}
  }
  
  public Map<String, Player> lobbylessMap() {
	  return notInLobbies;
  }
}
