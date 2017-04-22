package edu.brown.cs.jmrs.server;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.BiFunction;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import edu.brown.cs.jmrs.server.collections.ConcurrentBiMap;
import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.server.threading.GlobalThreadManager;

class ServerWorker extends WebSocketServer {

  private Server                             server;
  private LobbyManager                       lobbies;
  private ConcurrentBiMap<WebSocket, Player> players;
  private CommandInterpreter                 interpreter;

  public ServerWorker(
      Server server,
      int port,
      BiFunction<Server, String, ? extends Lobby> lobbyFactory,
      CommandInterpreter interpreter) {
    super(new InetSocketAddress(port));

    this.server = server;
    this.interpreter = interpreter;
    lobbies = new LobbyManager(lobbyFactory);
    players = new ConcurrentBiMap<>();
  }

  public String setPlayerId(WebSocket conn, String playerId) throws InputError {
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

  public Player getPlayer(WebSocket conn) {
    return players.get(conn);
  }

  public WebSocket getPlayer(String playerId) {
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

  public void playerDisconnected(WebSocket conn) {
    Player player = players.get(conn);
    if (player.getLobby() != null) {
      player.getLobby().removeClient(player.getId());
      player.setLobby(null);
    }
    players.remove(conn);
  }

  public void playerConnected(WebSocket conn) {
    players.put(conn, new Player(""));
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    GlobalThreadManager.submit(new PlayerDisconnectedHandler(this, conn));
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    ex.printStackTrace();
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    GlobalThreadManager
        .submit(new ServerCommandHandler(this, conn, message, interpreter));
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    GlobalThreadManager.submit(new PlayerConnectedHandler(this, conn));
  }
}
