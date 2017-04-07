package edu.brown.cs.jmrs.networkexperiment;

import org.java_websocket.WebSocket;

public class PlayerDisconnectedHandler implements Runnable {

  WebSocket conn;

  public PlayerDisconnectedHandler(WebSocket conn) {
    this.conn = conn;
  }

  @Override
  public void run() {
    ServerRunner server = ServerRunner.getInstance();

    Player player = server.getPlayers().get(conn);

    Lobby lobby = server.getLobbies().getLobbyOf(player);

    if (lobby != null) {
      player.setConnected(false);
    } else {
      ServerRunner.getInstance().getPlayers().remove(player);
    }
  }
}
