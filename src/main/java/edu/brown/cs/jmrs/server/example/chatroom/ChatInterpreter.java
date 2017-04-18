package edu.brown.cs.jmrs.server.example.chatroom;

import java.util.Map;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;

public class ChatInterpreter implements CommandInterpreter {

  public ChatInterpreter() {
  }

  @Override
  public void interpret(
      Lobby uncastLobby,
      String clientId,
      Map<String, ?> command) {
    ChatLobby lobby = (ChatLobby) uncastLobby;

    switch (((String) command.get("command")).toLowerCase()) {
      case "message":
        lobby.sendMessage(clientId, (String) command.get("message"));
        return;
      case "whisper":
        lobby.sendMessage(
            (String) command.get("recipient"),
            clientId,
            (String) command.get("message"));
        return;
    }
  }

}
