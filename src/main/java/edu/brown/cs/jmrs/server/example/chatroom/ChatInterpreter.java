package edu.brown.cs.jmrs.server.example.chatroom;

import java.util.Map;

import edu.brown.cs.jmrs.server.customizable.CommandInterpreter;
import edu.brown.cs.jmrs.server.customizable.Lobby;

public class ChatInterpreter implements CommandInterpreter {

  private enum Commands {
    MESSAGE, WHISPER
  }

  public ChatInterpreter() {
  }

  @Override
  public void interpret(
      Lobby uncastLobby,
      String clientId,
      Map<String, ?> command) {
    ChatLobby lobby = (ChatLobby) uncastLobby;

    String commandString = (String) command.get("command");

    switch (Commands.valueOf(commandString.toUpperCase())) {
      case MESSAGE:
        lobby.sendMessage(clientId, (String) command.get("message"));
        return;
      case WHISPER:
        lobby.sendMessage(
            (String) command.get("recipient"),
            clientId,
            (String) command.get("message"));
        return;
    }
  }

}
