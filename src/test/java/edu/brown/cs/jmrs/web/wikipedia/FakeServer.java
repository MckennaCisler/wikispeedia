package edu.brown.cs.jmrs.web.wikipedia;

import org.eclipse.jetty.websocket.api.Session;

import edu.brown.cs.jmrs.server.Server;

/**
 * A fake websocket server for testing, stripped of functionality.
 */
public class FakeServer extends Server {

  public FakeServer() {
    super(null, null, null);
  }

  @Override
  public void sendToClient(String clientId, String message) {
    // do nothing
    return;
  }

  @Override
  public void closeLobby(String lobbyId) {
    // do nothing
    return;
  }

  @Override
  public void onClose(Session conn, int code, String reason) {
    // do nothing
    return;
  }

  @Override
  public void onMessage(Session conn, String message) {
    // do nothing
    return;
  }
}
