package edu.brown.cs.jmrs.server;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;
import edu.brown.cs.jmrs.server.factory.Factory;

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
        this,
        port,
        new Factory<Lobby>(lobbyClass),
        new Factory<CommandInterpreter>(interpreterClass).get());
  }

  public Server(
      int port,
      Factory<? extends Lobby> lobbyFactory,
      CommandInterpreter interpreter) {
    server = new ServerWorker(this, port, lobbyFactory, interpreter);
  }

  public void start() {
    server.start();
  }

  public void sendToClient(String playerId, String message) {
    server.getPlayer(playerId).send(message);
  }
}
