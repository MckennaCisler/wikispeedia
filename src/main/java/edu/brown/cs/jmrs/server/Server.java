package edu.brown.cs.jmrs.server;

import edu.brown.cs.jmrs.factory.Factory;

/**
 * User access point to server functionality, keeps functionality black-boxed.
 * 
 * @author shastin1
 *
 */
public class Server {

  protected ServerWorker server;

  public Server(
      int port,
      Class<? extends Lobby> lobbyClass,
      Class<? extends CommandInterpreter> interpreterClass) {
    server = new ServerWorker(
        port,
        new Factory<Lobby>(lobbyClass),
        new Factory<CommandInterpreter>(interpreterClass));
  }

  public void start() {
    server.start();
  }
}
