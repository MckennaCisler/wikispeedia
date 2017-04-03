package edu.brown.cs.jmrs.networkexperiment;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class Server extends WebSocketServer {

  public Server(int port) throws UnknownHostException {
    super(new InetSocketAddress(port));
  }

  public Server(InetSocketAddress address) {
    super(address);
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    System.out
        .println("Currently " + connections().size() + " people in this chat.");
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    System.out
        .println("Currently " + connections().size() + " people in this chat.");
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    this.sendToAll(message);
    System.out.println(conn + ": " + message);
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    ex.printStackTrace();
    if (conn != null) {
      // some errors like port binding failed may not be assignable to a
      // specific websocket
    }
  }

  /**
   * Sends <var>text</var> to all currently connected WebSocket clients.
   * 
   * @param text
   *          The String to send across the network.
   * @throws InterruptedException
   *           When socket related I/O errors occur.
   */
  public void sendToAll(String text) {
    Collection<WebSocket> con = connections();
    synchronized (con) {
      for (WebSocket c : con) {
        c.send(text);
      }
    }
  }
}