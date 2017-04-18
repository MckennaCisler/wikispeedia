package edu.brown.cs.jmrs.server.example.chatroom;

import java.util.Map;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;

public class ChatInterpreter implements CommandInterpreter {

  ChatLobby lobby;
  String    playerId;

  public ChatInterpreter(String playerId, ChatLobby lobby) {
    this.playerId = playerId;
    this.lobby = lobby;
  }

  @Override
  public void interpret(
      Lobby uncastLobby,
      String clientId,
      Map<String, ?> command) {
    switch (((String) command.get("Command")).toLowerCase()) {
      case "message":
        lobby.sendMessage(playerId, (String) command.get("message"));
        return;
    }
  }

}
