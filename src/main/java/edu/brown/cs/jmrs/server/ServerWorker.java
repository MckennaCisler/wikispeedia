package edu.brown.cs.jmrs.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import edu.brown.cs.jmrs.server.customizable.core.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.core.Lobby;
import edu.brown.cs.jmrs.server.customizable.optional.CommandReformatter;
import edu.brown.cs.jmrs.server.factory.Factory;
import edu.brown.cs.jmrs.server.threading.GlobalThreadManager;

class ServerWorker extends WebSocketServer {

  private ConcurrentHashMap<String, Lobby> lobbies;
  protected ConcurrentHashMap<WebSocket, Player> players;
  private Factory<? extends Lobby> lobbyFactory;
  private Factory<? extends CommandInterpreter> interpreterFactory;
  private CommandReformatter commandReformatter;

  public ServerWorker(int port, Factory<? extends Lobby> lobbyFactory,
      Factory<? extends CommandInterpreter> interpreterFactory,
      Factory<? extends CommandReformatter> reformatterFactory) {
    super(new InetSocketAddress(port));

    this.lobbyFactory = lobbyFactory;
    this.interpreterFactory = interpreterFactory;
    players = new ConcurrentHashMap<>();
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

  public void setCommandReformatter(CommandReformatter commandReformatter) {
    this.commandReformatter = commandReformatter;
  }

  public CommandReformatter getCommandReformatter() {
    return commandReformatter;
  }

  public Player getPlayer(WebSocket conn) {
    return players.get(conn);
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
    Lobby lobby = lobbyFactory.getWithAdditionalArgs(lobbyId);
    lobbies.put(lobbyId, lobby);
    return lobby;
  }

  public CommandInterpreter bundleMessageForLobby(WebSocket conn) {
    return interpreterFactory.getWithAdditionalArgs(players.get(conn));
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
