package edu.brown.cs.jmrs.networkexperiment;

import org.java_websocket.WebSocket;

public class LobbyCommand implements Runnable {

  private WebSocket conn;
  private String    details;

  public LobbyCommand(WebSocket conn, String details) {
    this.conn = conn;
    this.details = details;
  }

  @Override
  public void run() {
    ServerRunner server = ServerRunner.getInstance();

    Player player = server.getPlayers().get(conn);

    // bad design, i know. Im just designing as I write because Im prototyping
    // not actually building the real project

    if (details.startsWith("start")) {
      server.getLobbies().removeFromLobby(player);
      server.getLobbies()
          .startLobby(player, details.substring("start".length()));
    } else if (details.equals("close lobby")) {
      server.getLobbies().closeLobbyOf(player);
    } else if (details.startsWith("message")) {
      Lobby lobby = server.getLobbies().getLobbyOf(player);

      if (lobby != null) {
        lobby.sendToPlayers(details.substring("message".length()));
      }
    } else if (details.startsWith("setname")) {
      player.setName(details.substring("setname".length()));
    } else if (details.startsWith("join")) {
      Lobby lobby = server.getLobbies().get(details.substring("join".length()));

      if (lobby == null) {
        player.messageClient("no such lobby exists");
      } else {
        lobby.add(player);
      }
    }
  }

}
