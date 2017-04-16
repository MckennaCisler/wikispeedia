package edu.brown.cs.jmrs.ui;

import com.google.common.collect.ImmutableList;

import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.server.example.chatroom.ChatInterpreter;
import edu.brown.cs.jmrs.server.example.chatroom.ChatLobby;
import edu.brown.cs.jmrs.wikispeedia.WikiHandlers;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Main {

  public static final int DEFAULT_SPARK_PORT = 4567;
  public static final int DEFAULT_SOCKET_PORT = 4568;

  public static void main(String[] args) {
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("spark-port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_SPARK_PORT);
    parser.accepts("socket-port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_SOCKET_PORT);
    OptionSet options = parser.parse(args);

    if (options.has("gui")) {
      Server server =
          new Server((int) options.valueOf("socket-port"), ChatLobby.class,
              ChatInterpreter.class);
      try {
        server.start();

        SparkServer.runSparkServer((int) options.valueOf("spark-port"),
            ImmutableList.of(new WikiHandlers()));
      } finally {
        SparkServer.stop();
      }
    }
  }
}
