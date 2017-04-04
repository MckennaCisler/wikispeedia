package edu.brown.cs.jmrs.networkexperiment;

import java.util.concurrent.Callable;

public class LobbyCommand implements Callable<String> {

  ServerRunner server;
  String       details;

  public LobbyCommand(ServerRunner server, String details) {
    this.server = server;
    this.details = details;
  }

  @Override
  public String call() {
    if (details.equals("start lobby")) {
      int port = server.generatePort();
      new Lobby(server, port).start();
      return "lobby started on port " + port;
    } else {
      return "message 'start lobby' to start a lobby");
    }
  }

}
