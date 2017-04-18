package edu.brown.cs.jmrs.server.customizable;

import java.util.Map;

public interface Lobby {

  void init(Map<String, ?> arguments);

  boolean isClosed();

  void addClient(String clientId);

  void removeClient(String clientId);
}
