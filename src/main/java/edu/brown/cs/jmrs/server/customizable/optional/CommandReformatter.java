package edu.brown.cs.jmrs.server.customizable.optional;

import java.util.List;

public interface CommandReformatter {

  void parse(String fromClient);

  String getCommand();

  List<String> getArgs();
}
