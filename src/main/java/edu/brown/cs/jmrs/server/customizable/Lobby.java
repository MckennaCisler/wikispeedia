package edu.brown.cs.jmrs.server.customizable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.brown.cs.jmrs.server.InputError;

public interface Lobby {

  void init(JsonObject jsonElement) throws InputError;

  boolean isClosed();

  void addClient(String clientId);

  void removeClient(String clientId);

  void playerReconnected(String clientId);

  void playerDisconnected(String clientId);

  JsonElement toJson(Gson gson);
}
