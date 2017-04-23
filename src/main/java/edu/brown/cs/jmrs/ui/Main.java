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
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

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
        System.out.println("[ Started Spark ]");

      } finally {
        SparkServer.stop();
      }
    }

    if (options.has("gui")) {
      Server server = new Server(
          (int) options.valueOf("socket-port"),
          (serv, str) -> {
            return new WikiLobby(serv, str);
          },
          new WikiInterpreter());
      server.start();
      System.out.println("[ Started Main GUI ]");

    } else if (options.has("chat-test")) {
      Spark.staticFileLocation("/public");
      SparkServer.runSparkServer(
          (int) options.valueOf("spark-port"),
          ImmutableList.of(new SparkHandlers() {

            @Override
            public void registerHandlers(FreeMarkerEngine freeMarker) {
              Spark.staticFileLocation("/public");
              Spark.get("/", new Route() {

                @Override
                public Object handle(Request request, Response response)
                    throws Exception {
                  response.redirect("/index.html");
                  return null;
                }

              });
            }

          }));

      Server server = new Server(
          (int) options.valueOf("socket-port"),
          (serv, str) -> {
            return new ChatLobby(serv, str);
          },
          new ChatInterpreter());
      server.start();
      System.out.println("[ Started Chat Test ]");
    }
  }
}
