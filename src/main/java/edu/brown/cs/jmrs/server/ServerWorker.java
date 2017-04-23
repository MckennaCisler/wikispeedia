package edu.brown.cs.jmrs.server;

import java.net.HttpCookie;
import java.util.List;
import java.util.function.BiFunction;

import org.eclipse.jetty.websocket.api.Session;

import edu.brown.cs.jmrs.server.collections.ConcurrentBiMap;
import edu.brown.cs.jmrs.server.customizable.Lobby;

class ServerWorker {

  private Server                           server;
  private LobbyManager                     lobbies;
  private ConcurrentBiMap<Session, Player> players;

  public ServerWorker(
      Server server,
      BiFunction<Server, String, ? extends Lobby> lobbyFactory) {
    this.server = server;
    lobbies = new LobbyManager(lobbyFactory);
    players = new ConcurrentBiMap<>();
  }

  public String setPlayerId(Session conn, String playerId) throws InputError {
    Lobby lobby = players.get(conn).getLobby();
    if (lobby == null) {
      if (playerId == null || playerId.length() == 0) {
        playerId = conn.hashCode() + "";
        Player player = new Player(playerId);
        while (!players.putNoOverwrite(conn, player)) {
          playerId = Math.random() + "";
          player = new Player(playerId);
        }
      } else {
        Player newPlayer = new Player(playerId);
        if (!players.putNoOverwrite(conn, newPlayer)) {
          throw new InputError("ID currently in use.");
        }
      }
      return playerId;
    } else {
      throw new InputError("Cannot change ID while in lobby.");
    }
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

  public void playerDisconnected(Session conn) {
    Player player = players.get(conn);
    if (player.getLobby() != null) {
      player.getLobby().removeClient(player.getId());
      player.setLobby(null);
    }
    players.remove(conn);
  }

  public void playerConnected(Session conn) {
    List<HttpCookie> cookies = conn.getUpgradeRequest().getCookies();
    for (HttpCookie cookie : cookies) {
      System.out.println(cookie.getName());
      System.out.println(cookie.getValue());
    }
    conn.getUpgradeResponse().addHeader("Set-Cookie", "clientid=fuckifIknow");
    conn.getUpgradeResponse().setHeader("Set-Cookie", "client__id=fuckifIknow");
    players.put(conn, new Player(""));
  }
}
