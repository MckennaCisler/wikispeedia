package edu.brown.cs.jmrs.server.customizable;

import com.google.gson.JsonObject;

public interface Lobby {

  void init(JsonObject jsonElement);

  boolean isClosed();

  void addClient(String clientId);

  void removeClient(String clientId);

  void playerReconnected(String clientId);

  void playerDisconnected(String clientId);

}
