package edu.brown.cs.jmrs.server;

import java.util.List;

import com.google.common.collect.ImmutableList;

class ClientCommand {

  private String   command;
  private String[] args;

  public String getCommand() {
    return command;
  }

  public List<String> getArgs() {
    return ImmutableList.copyOf(args);
  }
}
