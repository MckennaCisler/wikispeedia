package edu.brown.cs.jmrs.server;

import edu.brown.cs.jmrs.server.customizable.core.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.core.Lobby;
import edu.brown.cs.jmrs.server.customizable.optional.CommandReformatter;
import edu.brown.cs.jmrs.server.example.chatroom.SimpleJsonReformatter;
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
        port,
        new Factory<Lobby>(lobbyClass),
        new Factory<CommandInterpreter>(interpreterClass),
        new Factory<CommandReformatter>(SimpleJsonReformatter.class));
  }

  public Server(
      int port,
      Class<? extends Lobby> lobbyClass,
      Class<? extends CommandInterpreter> interpreterClass,
      Class<? extends CommandReformatter> reformatterClass) {
    server = new ServerWorker(
        port,
        new Factory<Lobby>(lobbyClass),
        new Factory<CommandInterpreter>(interpreterClass),
        new Factory<CommandReformatter>(reformatterClass));
  }

  public Server(
      int port,
      Factory<? extends Lobby> lobbyFactory,
      Factory<? extends CommandInterpreter> interpreterFactory) {
    server = new ServerWorker(
        port,
        lobbyFactory,
        interpreterFactory,
        new Factory<CommandReformatter>(SimpleJsonReformatter.class));
  }

  public Server(
      int port,
      Factory<? extends Lobby> lobbyFactory,
      Factory<? extends CommandInterpreter> interpreterFactory,
      Factory<? extends CommandReformatter> reformatterFactory) {
    server = new ServerWorker(
        port,
        lobbyFactory,
        interpreterFactory,
        reformatterFactory);
  }

  public void start() {
    server.start();
  }
}
