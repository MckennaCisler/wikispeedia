package edu.brown.cs.jmrs.server.example.chatroom;

import java.util.Map;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;

public class ChatInterpreter implements CommandInterpreter {

  ChatLobby lobby;
  String    playerId;

  public ChatInterpreter(String playerId, ChatLobby lobby) {
    this.playerId = playerId;
    this.lobby = lobby;
  }

  @Override
  public void interpret(Map<String, ?> command) {
    switch (((String) command.get("Command")).toLowerCase()) {
      case "message":
        lobby.sendMessage(playerId, (String) command.get("message"));
        return;
    }
  }

}
