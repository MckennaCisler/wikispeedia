package edu.brown.cs.jmrs.server.customizable;

public interface Lobby {

  boolean isClosed();

  void addPlayer(String playerId);
}
