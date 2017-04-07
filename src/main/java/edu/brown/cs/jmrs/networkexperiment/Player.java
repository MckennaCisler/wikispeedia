package edu.brown.cs.jmrs.networkexperiment;

import org.java_websocket.WebSocket;

public class Player {

  private WebSocket conn;
  private String    name;
  private boolean   connected;
  private String    lobbyName;

  public Player(WebSocket conn) {
    this.conn = conn;
    this.name = "anon";
    lobbyName = null;

    connected = true;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setLobby(String lobbyName) {
    this.lobbyName = lobbyName;
  }

  public String getLobby() {
    return lobbyName;
  }

  public String getName() {
    return name;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public void messageClient(String message) {
    if (connected) {
      conn.send(message);
    }
  }

  public void updateSocket(WebSocket conn) {
    this.conn = conn;
    connected = true;
  }
}
