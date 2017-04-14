package edu.brown.cs.jmrs.server;

public interface Lobby {

  String getId();

  void close();

  boolean isClosed();

  boolean hasPlayer(String playerId);

  void addPlayer(String playerId);
}
