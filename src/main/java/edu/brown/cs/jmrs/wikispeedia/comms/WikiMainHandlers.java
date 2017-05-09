package edu.brown.cs.jmrs.wikispeedia.comms;

import com.google.common.collect.ImmutableMap;

import edu.brown.cs.jmrs.ui.SparkHandlers;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.TemplateViewRoute;
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
    Spark.get("/", new StaticHandler("index.html"), freeMarker);
    Spark.get("/play", new StaticHandler("play.html"), freeMarker);
    Spark.get("/waiting", new StaticHandler("waiting.html"), freeMarker);
    Spark.get("/end", new StaticHandler("end.html"), freeMarker);
    Spark.get("/error", new StaticHandler("404.html"), freeMarker);
  }

  /**
   * A basic static redirection method (to remove .html file extension over
   * basic static files).
   *
   * @author mcisler
   *
   */
  public static final class StaticHandler implements TemplateViewRoute {
    private String filename;

    /**
     * @param filename
     *          The file (relative to static directory) to serve.
     */
    public StaticHandler(String filename) {
      this.filename = filename;
    }

    @Override
    public ModelAndView handle(Request req, Response res) {
      return new ModelAndView(ImmutableMap.of(), filename);
    }
  }
}
