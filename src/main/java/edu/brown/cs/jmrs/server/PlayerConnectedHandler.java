package edu.brown.cs.jmrs.server;

import org.eclipse.jetty.websocket.api.Session;

class PlayerConnectedHandler implements Runnable {

  private final ServerWorker server;
  private final Session conn;

  public PlayerConnectedHandler(ServerWorker server, Session conn) {
    this.server = server;
    this.conn = conn;
  }

  @Override
  public void run() {
    try {
      server.playerConnected(conn);
    } catch (Throwable e) {
      // solely for debugging purposes, as threads do not display exceptions
      // except when calling the value of an associated future:
      // http://stackoverflow.com/questions/2248131/
      // handling-exceptions-from-java-executorservice-tasks
      e.printStackTrace();
      throw e;
    }
  }
}
