package edu.brown.cs.jmrs.server;

import edu.brown.cs.jmrs.server.customizable.Lobby;

class Player {

  private String  id;
  private Lobby   lobby;
  private boolean connected;

  public Player(String id) {
    this.id = id;
    connected = true;
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

  @Override
  public boolean equals(Object o) {
    if (o instanceof Player) {
      Player po = (Player) o;

      if (po.getId().equals(getId())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  public boolean toggleConnected() {
    connected = !connected;
    return !connected;
  }

  public boolean isConnected() {
    return connected;
  }
}
