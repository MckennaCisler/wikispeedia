package edu.brown.cs.jmrs.server.customizable.core;

import java.util.List;

public interface CommandInterpreter {

  void interpret(String command, List<String> args);
}
