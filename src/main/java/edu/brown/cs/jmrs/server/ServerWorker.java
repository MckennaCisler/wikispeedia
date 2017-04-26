package edu.brown.cs.jmrs.server;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiFunction;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.collect.ConcurrentBiMap;
import edu.brown.cs.jmrs.server.customizable.Lobby;

class ServerWorker {

  private Server                           server;
  private LobbyManager                     lobbies;
  private ConcurrentBiMap<Session, Player> players;
  private Queue<Player>                    disconnectedPlayers;

  public ServerWorker(
      Server server,
      BiFunction<Server, String, ? extends Lobby> lobbyFactory) {
    this.server = server;
    lobbies = new LobbyManager(lobbyFactory);
    players = new ConcurrentBiMap<>();
    disconnectedPlayers = new PriorityBlockingQueue<>();
  }

  public String setPlayerId(Session conn, String playerId) throws InputError {
    Player player = players.getBack(new Player(playerId));
    if (player == null) {
      playerId = conn.hashCode() + "";
      player = new Player(playerId);
      while (!players.putNoOverwrite(conn, player)) { // TODO: NOT ADDING TO
                                                      // PLAYERS HERE???
        playerId = Math.random() + "";
        player = new Player(playerId);
      }

      players.put(conn, player); // I seemed to have to add this here for some
                                 // reason...

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
    int expiration = 0;
    List<HttpCookie> cookies = conn.getUpgradeRequest().getCookies();
    for (HttpCookie cookie : cookies) {
      if (cookie.getName().equals("client_id")) {
        String cookieVal = cookie.getValue();
        expiration = Integer
            .parseInt(cookieVal.substring(cookieVal.indexOf(":") + 1));
        break;
      }
    }

    if (expiration > 0) { // TODO: Do you mean greater than the current UNIX
                          // timestamp?
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

  private void checkDisconnectedPlayers() { // TODO: Why do you need this if is
                                            // removes a player that has expired
                                            // in the above function?
    if (!disconnectedPlayers.isEmpty()) {
      Player p = disconnectedPlayers.poll();
      while (p.getCookieExpiration() <= 0) { // TODO: Is cookie expiration
                                             // somehow updated (subtracted from
                                             // as time goes on)? How does it
                                             // expire?
                                             // Sean: lol whoops no i forgot
        players.remove(players.getReversed(p));
        if (!disconnectedPlayers.isEmpty()) {
          p = disconnectedPlayers.poll();
        }
      }
    }
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

    String toClient = "";

    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("command", "notify_id");
    try {
      try {
        String trueId = setPlayerId(conn, clientId);

        jsonObject.addProperty("client_id", trueId);
        jsonObject.addProperty("error_message", "");
        toClient = gson.toJson(jsonObject);
        conn.getRemote().sendString(toClient);

      } catch (InputError e) {
        jsonObject.addProperty("client_id", getPlayer(conn).getId());
        jsonObject.addProperty("error_message", e.getMessage());
        toClient = gson.toJson(jsonObject);
        conn.getRemote().sendString(toClient);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
