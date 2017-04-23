package edu.brown.cs.jmrs.ui;

import java.io.IOException;

import com.google.common.collect.ImmutableList;

import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.server.example.chatroom.ChatInterpreter;
import edu.brown.cs.jmrs.server.example.chatroom.ChatLobby;
import edu.brown.cs.jmrs.wikispeedia.WikiInterpreter;
import edu.brown.cs.jmrs.wikispeedia.WikiLobby;
import edu.brown.cs.jmrs.wikispeedia.WikiMainHandlers;
import edu.brown.cs.jmrs.wikispeedia.WikiPageHandlers;
import joptsimple.OptionException;
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
  public static final int DEFAULT_SPARK_PORT = 4567;
  public static final int DEFAULT_SOCKET_PORT = 4568;

  private Main() {
    // override default constructor
  }

  /**
   * Main execution method for Wikispeedia.
   *
   * @param args
   *          CLI args
   * @throws IOException
   *           If we can't print for some reason
   */
  public static void main(String[] args) throws IOException {
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("chat-test");
    parser.accepts("spark-port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_SPARK_PORT);
    parser.accepts("socket-port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_SOCKET_PORT);

    OptionSet options;
    try {
      options = parser.parse(args);
    } catch (OptionException ope) {
      System.out.println(ope.getMessage());
      parser.printHelpOn(System.out);
      return;
    }

    if (options.has("gui")) {
      try {
        // Setup Spark for main page and extra serving
        SparkServer.runSparkServer((int) options.valueOf("spark-port"),
            ImmutableList.of(new WikiMainHandlers(), new WikiPageHandlers()),
            "/static", "src/main/resources/public");
        System.out.println("[ Started Spark ]");

        // Setup websocket lobby server (which will use Spark)
        Server server =
            new Server((int) options.valueOf("socket-port"), (serv, str) -> {
              return new WikiLobby(serv, str);
            }, new WikiInterpreter());
        server.start();
        System.out.println("[ Started Main GUI ]");

      } finally {
        // SparkServer.stop(); // TODO: how to really stop it?
      }

    } else if (options.has("chat-test")) {
      SparkServer.runSparkServer((int) options.valueOf("spark-port"),
          ImmutableList.of(new SparkHandlers() {

            @Override
            public void registerHandlers(FreeMarkerEngine freeMarker) {
              Spark.get("/", new Route() {

                @Override
                public Object handle(Request request, Response response)
                    throws Exception {
                  response.redirect("index.html");
                  return "";
                }

              });
            }

          }), "/public", "src/main/resources/public");

      Server server =
          new Server((int) options.valueOf("socket-port"), (serv, str) -> {
            return new ChatLobby(serv, str);
          }, new ChatInterpreter());
      server.start();
      System.out.println("[ Started Chat Test ]");
    }
  }
}
