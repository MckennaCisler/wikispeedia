package edu.brown.cs.jmrs.ui;

import com.google.common.collect.ImmutableList;

import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.server.example.chatroom.ChatInterpreter;
import edu.brown.cs.jmrs.server.example.chatroom.ChatLobby;
import edu.brown.cs.jmrs.wikispeedia.WikiHandlers;
import edu.brown.cs.jmrs.wikispeedia.WikiInterpreter;
import edu.brown.cs.jmrs.wikispeedia.WikiLobby;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Primary execution class.
 *
 * @author mcisler
 *
 */
public final class Main {
  public static final int DEFAULT_SPARK_PORT  = 4567;
  public static final int DEFAULT_SOCKET_PORT = 4568;

  private Main() {
    // override default constructor
  }

  /**
   * Main execution method for Wikispeedia.
   *
   * @param args
   *          CLI args
   */
  public static void main(String[] args) {
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("spark");
    parser.accepts("chat-test");
    parser.accepts("spark-port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_SPARK_PORT);
    parser.accepts("socket-port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_SOCKET_PORT);
    OptionSet options = parser.parse(args);

    if (options.has("spark")) {
      try {
        SparkServer.runSparkServer(
            (int) options.valueOf("spark-port"),
            ImmutableList.of(new WikiHandlers()));

      } finally {
        SparkServer.stop();
      }
    }

    if (options.has("gui")) {
      Server server = new Server(
          (int) options.valueOf("socket-port"),
          (Server, String) -> {
            return new WikiLobby(Server, String, null, null, null);
          },
          new WikiInterpreter());
      server.start();

    } else if (options.has("chat-test")) {
      Server server = new Server(
          (int) options.valueOf("socket-port"),
          (Server, String) -> {
            return new ChatLobby(Server, String);
          },
          new ChatInterpreter());
      server.start();
    }
  }
}
