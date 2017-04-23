package edu.brown.cs.jmrs.server;

import org.eclipse.jetty.websocket.api.Session;

class PlayerConnectedHandler implements Runnable {

  ServerWorker server;
  Session      conn;

  public PlayerConnectedHandler(ServerWorker server, Session conn) {
    this.server = server;
    this.conn = conn;
  }

  @Override
  public void run() {
    server.playerConnected(conn);
  }
}
