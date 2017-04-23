package edu.brown.cs.jmrs.wikispeedia;

import edu.brown.cs.jmrs.ui.SparkHandlers;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * Registers main GUI handlers (basically redirects to static HTML) to the
 * SparkServer.
 *
 * @author mcisler
 *
 */
public class WikiMainHandlers implements SparkHandlers {

  @Override
  public void registerHandlers(FreeMarkerEngine freeMarker) {
    Spark.get("/", new StaticHandler("index.html"));
    Spark.get("/play", new StaticHandler("play.html"));
    Spark.get("/waiting", new StaticHandler("waiting.html"));
    Spark.get("/end", new StaticHandler("end.html"));
  }

  /**
   * A basic static redirection method (to remove .html file extension over
   * basic static files).
   *
   * @author mcisler
   *
   */
  public static final class StaticHandler implements Route {
    private String filename;

    /**
     * @param filename
     *          The file (relative to static directory) to serve.
     */
    public StaticHandler(String filename) {
      this.filename = filename;
    }

    @Override
    public String handle(Request req, Response res) {
      res.redirect(filename);
      return "";
    }
  }
}
