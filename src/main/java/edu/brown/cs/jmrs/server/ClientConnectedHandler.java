package edu.brown.cs.jmrs.server;

import org.eclipse.jetty.websocket.api.Session;

/**
 * Handles when a websocket connects.
 *
 * @author shastin1
 *
 */
class ClientConnectedHandler implements Runnable {

  private final ServerWorker server;
  private final Session      conn;

  /**
   * Constructor setting values to operate on when a thread is available.
   *
   * @param server
   *          The server connected to
   * @param conn
   *          The connection to the client
   */
  public ClientConnectedHandler(ServerWorker server, Session conn) {
    this.server = server;
    this.conn = conn;
  }

  @Override
  public void run() {
    try {
      server.clientConnected(conn);
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
