package edu.brown.cs.jmrs.server.example.chatroom;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.brown.cs.jmrs.server.customizable.optional.CommandReformatter;

public class SimpleJsonReformatter implements CommandReformatter {

  String       command;
  List<String> args;

  @Override
  public void parse(String fromClient) {
    Gson gson = new GsonBuilder().create();
    ClientCommand command = gson.fromJson(fromClient, ClientCommand.class);

    this.command = command.getCommand();
    args = ImmutableList.copyOf(command.getArgs());
  }

  @Override
  public String getCommand() {
    return command;
  }

  @Override
  public List<String> getArgs() {
    return args;
  }

  private class ClientCommand {

    private String   command;
    private String[] args;

    public String getCommand() {
      return command;
    }

    public String[] getArgs() {
      return args;
    }
  }
}
