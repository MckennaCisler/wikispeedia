package edu.brown.cs.jmrs.server.customizable;

import java.util.Map;

public interface CommandInterpreter {

  void interpret(Map<String, ?> command);
}
