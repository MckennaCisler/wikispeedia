package edu.brown.cs.jmrs.server.threading;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jetty.websocket.api.Session;

/**
 * Class for making all websocket traffic synchronous without blocking worker
 * threads.
 *
 * @author shastin1
 *
 */
public class MessageQueue {

  /**
   * Class storing message and it's destination for when the thread can get
   * around to sending the message.
   *
   * @author shastin1
   *
   */
  private class BundledMessage implements Runnable {
    private Session client;
    private String  message;

    /**
     * constructor specifying the client connection and message.
     *
     * @param client
     *          The client connection to send the message through
     * @param message
     *          The message to send through the client connection
     */
    public BundledMessage(Session client, String message) {
      this.client = client;
      this.message = message;
    }

    @Override
    public void run() {
      try {
        client.getRemote().sendString(message);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private ExecutorService exec = Executors.newFixedThreadPool(1);

  /**
   * Adds message and client connection to a queue to be sent when it is their
   * turn.
   *
   * @param client
   *          The client connection to send the message through
   * @param message
   *          The message to send through the client connection
   */
  public void send(Session client, String message) {
    exec.submit(new BundledMessage(client, message));
  }
}
