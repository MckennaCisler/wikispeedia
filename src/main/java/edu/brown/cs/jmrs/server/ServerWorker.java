package edu.brown.cs.jmrs.server;

import java.net.HttpCookie;
import java.util.List;
import java.util.function.BiFunction;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.collections.ConcurrentBiMap;
import edu.brown.cs.jmrs.server.customizable.Lobby;

class ServerWorker {

  private Server server;
  private LobbyManager lobbies;
  private ConcurrentBiMap<Session, Player> players;

  public ServerWorker(Server server,
      BiFunction<Server, String, ? extends Lobby> lobbyFactory) {
    this.server = server;
    lobbies = new LobbyManager(lobbyFactory);
    players = new ConcurrentBiMap<>();
  }

  public String setPlayerId(Session conn, String playerId) throws InputError {
    Player player = players.get(conn);
    Lobby lobby = player == null ? null : player.getLobby();
    if (player == null || player.getLobby() == null || (!player.isConnected()
        && !(playerId == null || playerId.length() == 0))) {
      // initial setting of id OR can change it freely if not in lobby OR can
      // set id on reconnect
      if (playerId == null || playerId.length() == 0) {
        playerId = conn.hashCode() + "";
        player = new Player(playerId);
        while (!players.putNoOverwrite(conn, player)) {
          playerId = Math.random() + "";
          player = new Player(playerId);
        }
      } else if (players.containsValue(playerId)
          && (player == null || player.isConnected())) {
        throw new InputError("client id '" + playerId + "' already taken");
      } else {
        player = new Player(playerId);
      }
    } else if (player.isConnected()) { // trying to change id while in lobby:
                                       // UNACCEPTABLE!!!!!
      throw new InputError("Cannot change client id while in a lobby");
    } else {
      throw new InputError("Cannot set a random id on reconnect");
    }

    player.setLobby(lobby);
    players.put(conn, player);
    if (lobby != null) {
      lobby.playerReconnected(player.getId());
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
    Player player = players.get(conn);
    if (player.getLobby() != null && player.isConnected()) {
      player.getLobby().playerDisconnected(player.getId());
      player.toggleConnected();
    } else {
      players.remove(conn);
    }
  }

  public void playerConnected(Session conn) {
    String id = "";
    List<HttpCookie> cookies = conn.getUpgradeRequest().getCookies();
    for (HttpCookie cookie : cookies) {
      System.out.println(cookie.getName());
      if (cookie.getName().equals("client_id")) {
        id = cookie.getValue();
        break;
      }
    }

    Gson gson = new Gson();
    JsonObject payload = new JsonObject();
    payload.addProperty("client_id", id);
    JsonObject response = new JsonObject();
    response.addProperty("command", "set_client_id");
    response.add("payload", payload);

    new ServerCommandHandler(this, conn, gson.toJson(response), null).run();
  }
}
