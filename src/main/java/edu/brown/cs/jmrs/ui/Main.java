package edu.brown.cs.jmrs.ui;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.brown.cs.jmrs.collect.graph.DirectedDijkstraPathFinder;
import edu.brown.cs.jmrs.collect.graph.Graph.PathFinder;
import edu.brown.cs.jmrs.io.db.DbConn;
import edu.brown.cs.jmrs.server.Server;
import edu.brown.cs.jmrs.web.FailSafeLinkFInder;
import edu.brown.cs.jmrs.web.Link;
import edu.brown.cs.jmrs.web.Page;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder.Filter;
import edu.brown.cs.jmrs.wikispeedia.Scraper;
import edu.brown.cs.jmrs.wikispeedia.WikiLobby;
import edu.brown.cs.jmrs.wikispeedia.WikiPath;
import edu.brown.cs.jmrs.wikispeedia.WikiPath.Visit;
import edu.brown.cs.jmrs.wikispeedia.WikiPlayer;
import edu.brown.cs.jmrs.wikispeedia.comms.WikiInterpreter;
import edu.brown.cs.jmrs.wikispeedia.comms.WikiMainHandlers;
import edu.brown.cs.jmrs.wikispeedia.comms.WikiPageHandlers;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.Spark;

/**
 * Primary execution class for Wikispeedia.
 *
 * @author mcisler
 *
 */
public final class Main {
  public static final int     DEFAULT_SPARK_PORT  = 4567;
  public static final int     DEFAULT_SOCKET_PORT = 4568;
  public static final boolean DEBUG               = false;
  public static final boolean VERBOSE_LOG         = true;

  /**
   * Global GSON for defining custom JSON serializers on.
   */
  public static final Gson GSON = registerSerializers();

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

  /**
   * Cache for the internals of WikiPages.
   */
  static final int MAX_WIKI_CACHE_SIZE       = 40;
  static final int WIKIPAGE_EVICTION_TIMEOUT = 12; // hours

  public static final LoadingCache<String, Document> WIKI_PAGE_DOC_CACHE =
      CacheBuilder.newBuilder().maximumSize(MAX_WIKI_CACHE_SIZE)
          .expireAfterWrite(WIKIPAGE_EVICTION_TIMEOUT, TimeUnit.HOURS)
          .build(new Page.Loader());

  /**
   * DbConn and constants for database Link cache and associated LinkFinder.
   */
  static final String WIKI_DATABASE_LOC = "data/wikipedia.sqlite3";

  private static DbConn wikiDbConn;

  private Main() {
    // override default constructor
  }

  /**
   * Main execution method for Wikispeedia.
   *
   * @param args
   *          CLI args
   * @throws Exception
   *           If something can't close for some reason.
   */
  public static void main(String[] args) throws Exception {
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("spark-port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_SPARK_PORT);
    parser.accepts("chat-test");
    parser.accepts("scrape");
    parser.accepts("scrape-start").withRequiredArg().ofType(String.class)
        .defaultsTo("Main_Page");
    parser.accepts("scrape-method").withRequiredArg().ofType(String.class)
        .defaultsTo("breadth").describedAs("breadth|random-descent");
    parser.accepts("scrape-depth").withRequiredArg().ofType(Integer.class)
        .defaultsTo(-1);
    parser.accepts("scrape-only-english");
    parser.accepts("shortest-path");
    parser.accepts("start").requiredIf("shortest-path").withRequiredArg()
        .ofType(String.class);
    parser.accepts("end").requiredIf("shortest-path").withRequiredArg()
        .ofType(String.class);

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
        // Open database
        wikiDbConn = new DbConn(WIKI_DATABASE_LOC);
        System.out.println("[ Opened Database ]");

        // Setup websocket lobby server (which will use Spark)
        Server server = new Server((serv, str) -> {
          return new WikiLobby(serv, str);
        }, new WikiInterpreter(), GSON);
        Spark.webSocket("/websocket", server);
        System.out.println("[ Started Websocket ]");

        // set page serving debug state
        SparkServer.setDebug(DEBUG);

        // Setup Spark for main page and optionally debug handlers
        List<SparkHandlers> handlers = new ArrayList<>();
        handlers.add(new WikiMainHandlers());
        if (DEBUG) {
          handlers.add(new WikiPageHandlers());
        }

        SparkServer.runSparkServer((int) options.valueOf("spark-port"),
            handlers, "/static", "src/main/resources/public");
        System.out.println("[ Started Spark ]");

        String waiter = "";
        synchronized (waiter) {
          while (true) {
            waiter.wait();
          }
        }

      } catch (InterruptedException e) {
      } catch (ClassNotFoundException | FileNotFoundException e) {
        System.out.println("Could not open database: " + e.getMessage());
      } finally {
        SparkServer.stop();
        wikiDbConn.close();
      }
    } else if (options.has("scrape")) {
      try {
        wikiDbConn = new DbConn(WIKI_DATABASE_LOC);
        System.out.println("[ Opened Database ]");

        // set optional filters for links
        List<Filter> filters = new ArrayList<>();
        if (options.has("scrape-only-english")) {
          filters.add(Filter.NON_ENGLISH_WIKIPEDIA);
        }

        Scraper scraper =
            new Scraper(wikiDbConn,
                WikiPage.fromAny((String) options.valueOf("scrape-start")),
                filters.toArray(new Filter[0]));

        scraper.setDepth((int) options.valueOf("scrape-depth"));

        switch ((String) options.valueOf("scrape-method")) {
          case "breadth":
            scraper.startBreadthFirstScrape();
            break;
          case "random-descent":
            scraper.startRandomDescentScrape();
            break;
          default:
            System.out.println("Invalid scrape option specified");
            parser.printHelpOn(System.out);

        }
      } finally {
        if (wikiDbConn != null) {
          wikiDbConn.close();
        }
      }
    } else if (options.has("shortest-path")) {

      PathFinder<Page, Link> edgeFinder =
          new DirectedDijkstraPathFinder<>(
              new FailSafeLinkFInder<WikiPage>(WikiLobby.DEFAULT_LINK_FINDER));

      // must have these two under shortest-path
      WikiPage start = WikiPage.fromAny((String) options.valueOf("start"));
      WikiPage end = WikiPage.fromAny((String) options.valueOf("end"));
      System.out.printf("Finding path between '%s' and '%s'%n", start, end);

      long startTime = System.currentTimeMillis();
      List<Link> links = edgeFinder.shortestPath(start, end);

      System.out.printf("Founds path in %d seconds: %n",
          (System.currentTimeMillis() - startTime) / 1000);
      for (Link link : links) {
        System.out.println(link);
      }
    }
  }

  /**
   * Prints the input string to a logging location for debugging.
   *
   * @param info
   *          The info message to log.
   */
  public static void debugLog(String info) {
    if (VERBOSE_LOG) {
      System.out.println(String.format("[ DEBUG : %s ] %s\n\r",
          new SimpleDateFormat("dd-MM HH:mm:ss").format(new Date()), info));
    }
  }

  /**
   * Prints the input string to a logging location for debugging.
   *
   * @param obj
   *          The object to log.
   */
  public static void debugLog(Object obj) {
    if (obj != null) {
      debugLog(obj.toString());
    }
  }

  /**
   * @return The current dbConn, ensured not to be null.
   */
  public static synchronized DbConn getWikiDbConn() {
    assert wikiDbConn != null;
    return wikiDbConn;
  }
}
