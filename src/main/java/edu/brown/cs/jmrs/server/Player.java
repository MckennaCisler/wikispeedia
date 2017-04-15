package edu.brown.cs.jmrs.server;

import edu.brown.cs.jmrs.server.customizable.core.Lobby;

class Player {

  private String id;
  private Lobby  lobby;

  public Player(String id) {
    this.id = id;
  }

  public void setLobby(Lobby lobby) {
    this.lobby = lobby;
  }

  public String getId() {
    return id;
  }

  public Lobby getLobby() {
    return lobby;
  }
}
