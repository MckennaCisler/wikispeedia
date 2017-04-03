package edu.brown.cs.jmrs.networkexperiment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.java_websocket.WebSocketImpl;

public class ServerRunner {

  public ServerRunner() {
    try {
      WebSocketImpl.DEBUG = true;
      Server s = new Server(4567);
      s.start();
      System.out.println("ChatServer started on port: " + s.getPort());

      BufferedReader sysin = new BufferedReader(
          new InputStreamReader(System.in));
      while (true) {
        String in = sysin.readLine();
        s.sendToAll(in);
        if (in.equals("exit")) {
          s.stop();
          break;
        }
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
