package edu.brown.cs.jmrs.server;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class ServerCommandHandler implements Runnable {

  ServerWorker server;
  WebSocket    conn;
  String       commandJson;

  public ServerCommandHandler(
      ServerWorker server,
      WebSocket conn,
      String command) {
    this.server = server;
    this.conn = conn;
    this.commandJson = command;
  }

  @Override
  public void run() {

    Gson gson = new GsonBuilder().create();
    ClientCommand command = gson.fromJson(commandJson, ClientCommand.class);

    Player player = server.getPlayer(conn);

    if (player.getId().length() > 0) {

      // do server commands here
      switch (command.getCommand()) {
        case "setPlayerId":
          if (command.getArgs().size() == 1) {
            if (server.setPlayerId(conn, command.getArgs().get(0))) {
              conn.send("SUCCESS");
            } else {
              conn.send("ERROR: ID taken");
            }
          } else {
            conn.send("ERROR: bad arguments");
          }
          return;
        case "startLobby":
          if (command.getArgs().size() == 1) {
            Lobby lobby = server.createLobby(command.getArgs().get(0));
            if (lobby != null) {
              lobby.addPlayer(player.getId());
            } else {
              conn.send("ERROR: ID taken");
            }
          } else {
            conn.send("ERROR: bad arguments");
          }
          return;
      }

      // if not a server command pass it to the lobby
      server.bundleMessageForLobby(conn)
          .interpret(command.getCommand(), command.getArgs());
    } else {
      conn.send("ERROR: must set ID first");
    }
  }
}
