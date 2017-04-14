package edu.brown.cs.jmrs.server;

import org.java_websocket.WebSocket;

class PlayerDisconnectedHandler implements Runnable {

  ServerWorker server;
  WebSocket    conn;

  public PlayerDisconnectedHandler(ServerWorker server, WebSocket conn) {
    this.server = server;
    this.conn = conn;
  }

  @Override
  public void run() {
    server.playerDisconnected(conn);
  }
}
