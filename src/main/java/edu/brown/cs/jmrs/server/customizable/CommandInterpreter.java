package edu.brown.cs.jmrs.server.customizable;

import com.google.gson.JsonObject;

public interface CommandInterpreter {

  void interpret(Lobby uncastLobby, String clientId, JsonObject command);
}
