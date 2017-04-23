package edu.brown.cs.jmrs.server;

import org.eclipse.jetty.websocket.api.Session;

class PlayerDisconnectedHandler implements Runnable {

  ServerWorker server;
  Session      conn;

  public PlayerDisconnectedHandler(ServerWorker server, Session conn) {
    this.server = server;
    this.conn = conn;
  }

  @Override
  public void run() {
    server.playerDisconnected(conn);
  }
}
