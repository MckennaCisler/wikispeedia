package edu.brown.cs.jmrs.wikispeedia;

import java.util.Map;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;

/**
 * Interprets commands over a Wiki lobby.
 *
 * @author mcisler
 *
 */
public class WikiInterpreter implements CommandInterpreter {

  /**
   * All possible commands for the WikiInterpreter.
   *
   * @author mcisler
   *
   */
  private enum Commands {

  }

  @Override
  public void interpret(
      Lobby uncastLobby,
      String clientId,
      Map<String, ?> command) {
    // TODO Auto-generated method stub

  }
}
