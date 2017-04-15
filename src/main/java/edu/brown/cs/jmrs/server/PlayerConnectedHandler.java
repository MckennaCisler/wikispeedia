package edu.brown.cs.jmrs.server;

import org.java_websocket.WebSocket;

class PlayerConnectedHandler implements Runnable {

  ServerWorker server;
  WebSocket    conn;

  public PlayerConnectedHandler(ServerWorker server, WebSocket conn) {
    this.server = server;
    this.conn = conn;
  }

  @Override
  public void run() {
    server.playerConnected(conn);
  }
}
