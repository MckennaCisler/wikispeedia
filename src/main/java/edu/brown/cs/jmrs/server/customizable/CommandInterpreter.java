package edu.brown.cs.jmrs.server.customizable;

import com.google.gson.JsonObject;

/**
 * Command interpreter for interpreting commands from the client.
 *
 * @author shastin1
 *
 */
public interface CommandInterpreter {

  /**
   * Interprets and acts on a given command.
   *
   * @param uncastLobby
   *          The lobby the client that sent the command is in
   * @param clientId
   *          The id of the client that send the command
   * @param command
   *          The command sent by the client
   */
  void interpret(Lobby uncastLobby, String clientId, JsonObject command);
}
