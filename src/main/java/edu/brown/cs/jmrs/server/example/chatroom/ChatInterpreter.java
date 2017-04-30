package edu.brown.cs.jmrs.server.example.chatroom;

import com.google.gson.JsonObject;

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
      JsonObject command) {
    ChatLobby lobby = (ChatLobby) uncastLobby;
    JsonObject payload = command.get("payload").getAsJsonObject();

    String commandString = command.get("command").getAsString();

    switch (Commands.valueOf(commandString.toUpperCase())) {
      case MESSAGE:
        lobby.sendMessage(clientId, payload.get("message").getAsString());
        return;
      case WHISPER:
        lobby.sendMessage(
            payload.get("recipient").getAsString(),
            clientId,
            payload.get("message").getAsString());
        return;
    }
  }

}
