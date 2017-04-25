package edu.brown.cs.jmrs.ui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import freemarker.template.Configuration;
import spark.ExceptionHandler;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * Class to contain the webserver-related methods for Stars, including handlers,
 * initialization, template handling, and shutdown code.
 *
 * @author mcisler
 *
 */
public final class SparkServer {
  private static boolean debug = false;

  /**
   * Overrides (and thus restricts) default constructor.
   */
  private SparkServer() {
    return;
  }

  /**
   * Starts up the SparkServer.
   *
   * @param port
   *          The port to bind the server to
   * @param handlers
   *          The handlers to ask for registrations.
   * @param staticFileLoc
   *          The location to server static files from, under
   *          src/main/resources/
   * @param freemarkerLoc
   *          Freemarker template loading location.
   */
  public static void runSparkServer(int port, List<SparkHandlers> handlers,
      String staticFileLoc, String freemarkerLoc) {
    Spark.port(port);

    if (debug) {
      String projectDir =
          System.getProperty("user.dir") + "/src/main/resources";
      System.out.println(projectDir + staticFileLoc);
      Spark.externalStaticFileLocation(projectDir + staticFileLoc);
    } else {
      Spark.staticFileLocation(staticFileLoc);
    }

    Spark.exception(Exception.class, new ExceptionPrinter());

    FreeMarkerEngine freeMarker = createEngine(freemarkerLoc);

    // Setup Routes
    for (SparkHandlers handler : handlers) {
      handler.registerHandlers(freeMarker);
    }
  }

  /**
   * Sets up a new FreeMarker engine.
   */
  private static FreeMarkerEngine createEngine(String freemarkerLoc) {
    Configuration config = new Configuration();
    File templates = new File(freemarkerLoc);
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      stop();
    }
    return new FreeMarkerEngine(config);
  }

  /**
   * Display an error page when an exception occurs in the server.
   *
   * @author jj
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }

  }

  /**
   * Checks if a value in a query map is valid.
   *
   * @param qm
   *          The QueryParamsMap to check for val's membership
   * @param val
   *          The value to check
   * @return true if the value is valid, else false.
   */
  public static boolean isValid(QueryParamsMap qm, String val) {
    return qm.hasKey(val) && !qm.value(val).equals("");
  }

  /**
   * Stops the current SparkServer.
   */
  public static void stop() {
    Spark.stop();
  }

  /**
   * Halts the SparkServer with the given message.
   *
   * @param msg
   *          The message.
   * @return A formal message.
   */
  public static String reqError(String msg) {
    String fullMsg = String.format("Request error: %s", msg);
    Spark.halt(400, fullMsg);
    return fullMsg;
  }

  /**
   * Sets the debug state, i.e. whether to cache or auto-update static files.
   *
   * @param val
   *          The new debug state.
   */
  public static void setDebug(boolean val) {
    debug = val;
  }
}
