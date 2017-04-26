package edu.brown.cs.jmrs.server;

import edu.brown.cs.jmrs.server.customizable.Lobby;

class Player implements Comparable<Player> {

  private String  id;
  private Lobby   lobby;
  private boolean connected;
  private int     cookieExpiration;

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
    if (connected) {
      connected = false;
      return true;
    } else {
      connected = true;
      return false;
    }
  }

  public boolean isConnected() {
    return connected;
  }

  public void setCookieExpiration(int date) {
    this.cookieExpiration = date;
  }

  public int getCookieExpiration() {
    return cookieExpiration;
  }

  @Override
  public int compareTo(Player p) {
    return cookieExpiration - p.getCookieExpiration();
  }
}
