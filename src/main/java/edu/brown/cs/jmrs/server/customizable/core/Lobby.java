package edu.brown.cs.jmrs.server.customizable.core;

public interface Lobby {

  String getId();

  void close();

  boolean isClosed();

  boolean hasPlayer(String playerId);

  void addPlayer(String playerId);

  // remove player ??
}
