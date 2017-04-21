package edu.brown.cs.jmrs.server;

import java.util.function.BiFunction;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;

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
      BiFunction<Server, String, ? extends Lobby> lobbyFactory,
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
