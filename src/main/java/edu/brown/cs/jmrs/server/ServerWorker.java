package edu.brown.cs.jmrs.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import edu.brown.cs.jmrs.server.collections.ConcurrentBiMap;
import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.server.factory.Factory;
import edu.brown.cs.jmrs.server.threading.GlobalThreadManager;

class ServerWorker extends WebSocketServer {

  private Server                                server;
  private ConcurrentHashMap<String, Lobby>      lobbies;
  protected ConcurrentBiMap<WebSocket, Player>  players;
  private Factory<? extends Lobby>              lobbyFactory;
  private Factory<? extends CommandInterpreter> interpreterFactory;

  public ServerWorker(
      Server server,
      int port,
      Factory<? extends Lobby> lobbyFactory,
      Factory<? extends CommandInterpreter> interpreterFactory) {
    super(new InetSocketAddress(port));

    this.server = server;
    this.lobbyFactory = lobbyFactory;
    this.interpreterFactory = interpreterFactory;
    players = new ConcurrentBiMap<>();
  }

  public boolean setPlayerId(WebSocket conn, String playerId) {
    Player newPlayer = new Player(playerId);
    if (players.containsValue(newPlayer)) {
      return false;
    } else {
      players.put(conn, newPlayer);
      return true;
    }
  }

  public Player getPlayer(WebSocket conn) {
    return players.get(conn);
  }

  public WebSocket getPlayer(String playerId) {
    return players.getReversed(new Player(playerId));
  }

  public synchronized Lobby createLobby(String lobbyId) {
    if (lobbies.containsKey(lobbyId)) {
      Lobby lobby = lobbies.get(lobbyId);
      if (lobby.isClosed()) {
        lobbies.remove(lobbyId);
      } else {
        return null;
      }
    }
    Lobby lobby = lobbyFactory.getWithAdditionalArgs(server, lobbyId);
    lobbies.put(lobbyId, lobby);
    return lobby;
  }

  public CommandInterpreter bundleMessageForLobby(WebSocket conn) {
    Player player = players.get(conn);
    return interpreterFactory
        .getWithAdditionalArgs(player.getId(), player.getLobby());
  }

  public void playerDisconnected(WebSocket conn) {
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
    GlobalThreadManager.submit(new ServerCommandHandler(this, conn, message));
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    GlobalThreadManager.submit(new PlayerConnectedHandler(this, conn));
  }
}
