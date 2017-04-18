package edu.brown.cs.jmrs.server.customizable;

import java.util.Map;

public interface CommandInterpreter {

  void interpret(Lobby uncastLobby, String clientId, Map<String, ?> command);
}
