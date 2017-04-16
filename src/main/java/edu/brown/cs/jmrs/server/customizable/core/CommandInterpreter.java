package edu.brown.cs.jmrs.server.customizable.core;

import java.util.List;

public interface CommandInterpreter {

  // Use Collection here instead?
  void interpret(String command, List<String> args);
}
