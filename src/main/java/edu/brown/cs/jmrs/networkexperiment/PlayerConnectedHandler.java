package edu.brown.cs.jmrs.networkexperiment;

import org.java_websocket.WebSocket;

public class PlayerConnectedHandler implements Runnable {

  WebSocket conn;

  public PlayerConnectedHandler(WebSocket conn) {
    this.conn = conn;
  }

  @Override
  public void run() {
    PlayerManager players = ServerRunner.getInstance().getPlayers();

    Player player = players.get(conn);

    if (player == null) {
      players.put(conn, new Player(conn));
    } else {
      player.setConnected(true);
    }
  }
}
