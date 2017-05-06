package edu.brown.cs.jmrs.server.threading;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jetty.websocket.api.Session;

public class MessageQueue {

  private ExecutorService exec = Executors.newFixedThreadPool(1);

  public void send(Session client, String message) {
    exec.submit(new BundledMessage(client, message));
  }

  private class BundledMessage implements Runnable {
    private Session client;
    private String  message;

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
}
