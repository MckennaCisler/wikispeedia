package edu.brown.cs.jmrs.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.server.example.chatroom.ChatInterpreter;
import edu.brown.cs.jmrs.server.example.chatroom.ChatLobby;
import edu.brown.cs.jmrs.web.Page;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.wikispeedia.WikiInterpreter;
import edu.brown.cs.jmrs.wikispeedia.WikiLobby;
import edu.brown.cs.jmrs.wikispeedia.WikiMainHandlers;
import edu.brown.cs.jmrs.wikispeedia.WikiPageHandlers;
import edu.brown.cs.jmrs.wikispeedia.WikiPath;
import edu.brown.cs.jmrs.wikispeedia.WikiPath.Visit;
import edu.brown.cs.jmrs.wikispeedia.WikiPlayer;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * Primary execution class for Wikispeedia.
 *
 * @author mcisler
 *
 */
public final class Main {
  public static final int DEFAULT_SPARK_PORT = 4567;
  public static final int DEFAULT_SOCKET_PORT = 4568;
  public static final boolean DEBUG = true;

  /**
   * Global GSON for defining custom JSON serializers on.
   */
  public static final Gson GSON = registerSerializers();

  /**
   * Cache for the internals of WikiPages.
   */
  static final int MAX_WIKI_CACHE_SIZE = 2000;
  static final int WIKIPAGE_EVICTION_TIMEOUT = 24; // hours
  public static final LoadingCache<String, Document> WIKI_PAGE_DOC_CACHE =
      CacheBuilder.newBuilder().maximumSize(MAX_WIKI_CACHE_SIZE)
          .expireAfterWrite(WIKIPAGE_EVICTION_TIMEOUT, TimeUnit.HOURS)
          .build(new Page.Loader());

  /**
   * Registers custom Json (Gson) serializers for this project.
   *
   * https://github.com/google/gson/blob/master/
   * UserGuide.md#TOC-Custom-Serialization-and-Deserialization
   *
   * @return A Gson Object with the register Serializers.
   */
  private static Gson registerSerializers() {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(WikiPage.class, new WikiPage.Serializer());
    builder.registerTypeAdapter(WikiLobby.class, new WikiLobby.Serializer());
    builder.registerTypeAdapter(WikiPlayer.class, new WikiPlayer.Serializer());
    builder.registerTypeAdapter(Visit.class, new WikiPath.VisitSerializer());

    return builder.create();
  }

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
        // Setup websocket lobby server (which will use Spark)
        Server server = new Server((serv, str) -> {
          return new WikiLobby(serv, str);
        }, new WikiInterpreter(), GSON);
        Spark.webSocket("/websocket", server);
        System.out.println("[ Started Websocket ]");

        // Setup Spark for main page and extra serving
        SparkServer.setDebug(DEBUG);
        SparkServer.runSparkServer((int) options.valueOf("spark-port"),
            ImmutableList.of(new WikiMainHandlers(), new WikiPageHandlers()),
            "/static", "src/main/resources/public");
        System.out.println("[ Started Spark ]");

        // TODO: how to really stop it?
        String waiter = "";
        synchronized (waiter) {
          while (true) {
            waiter.wait();
          }
        }

      } catch (InterruptedException e) {
      } finally {
        SparkServer.stop();
      }
    } else if (options.has("chat-test")) {

      Server server = new Server((serv, str) -> {
        return new ChatLobby(serv, str);
      }, new ChatInterpreter(), new Gson());
      Spark.webSocket("/websocket", server);

      SparkServer.runSparkServer((int) options.valueOf("spark-port"),
          ImmutableList.of(new SparkHandlers() {

            @Override
            public void registerHandlers(FreeMarkerEngine freeMarker) {
              Spark.get("/", new Route() {

                @Override
                public Object handle(Request request, Response response)
                    throws Exception {
                  response.redirect("index.html");
                  return null;
                }

              });
            }
          }), "/public-testing", "src/main/resources/public-testing");
      System.out.println("[ Started Chat Test ]");
    }
  }

  /**
   * Prints the input string to a logging location for debugging.
   *
   * @param info
   *          The info message to log.
   */
  public static void debugLog(String info) {
    if (DEBUG) {
      System.out.println(String.format("[ DEBUG : %s ]\n\r\t%s\n\r",
          new SimpleDateFormat("dd-MM HH:mm:ss").format(new Date()), info));
    } else {
      // TODO: Pipe to logfile
    }
  }

  /**
   * Prints the input string to a logging location for debugging.
   *
   * @param obj
   *          The object to log.
   */
  public static void debugLog(Object obj) {
    debugLog(obj.toString());
  }
}
