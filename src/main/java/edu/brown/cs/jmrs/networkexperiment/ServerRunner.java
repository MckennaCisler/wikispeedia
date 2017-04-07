package edu.brown.cs.jmrs.networkexperiment;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class ServerRunner extends WebSocketServer {

  private static ServerRunner instance;
  private static final int    DEFAULT_PORT = 4567;

  private ExecutorService     daemonManager;
  private LobbyManager        lobbies;
  private PlayerManager       players;

  public ServerRunner(int port) {
    super(new InetSocketAddress(port));

    int maxDaemons = Runtime.getRuntime().availableProcessors() - 1;
    daemonManager = Executors.newFixedThreadPool(maxDaemons);

    lobbies = new LobbyManager();

    instance = this;
  }

  public static ServerRunner getInstance() {
    if (instance == null) {
      instance = new ServerRunner(DEFAULT_PORT);
    }
    return instance;
  }

  public LobbyManager getLobbies() {
    return lobbies;
  }

  public PlayerManager getPlayers() {
    return players;
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    daemonManager.submit(new PlayerDisconnectedHandler(conn));
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    ex.printStackTrace();
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    daemonManager.submit(new LobbyCommand(conn, message));
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    daemonManager.submit(new PlayerConnectedHandler(conn));
  }
}
