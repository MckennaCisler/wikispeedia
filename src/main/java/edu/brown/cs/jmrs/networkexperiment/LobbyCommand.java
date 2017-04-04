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
    int port = server.generatePort();
    new Lobby(server, port);
    return port + "";
  }

}
