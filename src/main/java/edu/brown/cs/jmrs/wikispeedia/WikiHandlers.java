package edu.brown.cs.jmrs.wikispeedia;

import java.io.IOException;

import edu.brown.cs.jmrs.ui.SparkHandlers;
import edu.brown.cs.jmrs.ui.SparkServer;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.web.wikipedia.WikiPageLinkFinder;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * Holds handlers related to wikipedia page querying.
 *
 * @author mcisler
 *
 */
public class WikiHandlers implements SparkHandlers {
  private static LinkFinder<WikiPage> linkFinder = new WikiPageLinkFinder();

  @Override
  public void registerHandlers(FreeMarkerEngine freeMarker) {
    Spark.get("/wiki/:name", new InnerContentHandler());
    Spark.get("/wiki/links/:name", new LinksHandler());

    // TODO - Spark.get("/wiki/suggest/:page", new PageNameSuggestHandler());
  }

  /**
   * Handler for getting the inner content of a wiki page.
   *
   * @author mcisler
   *
   */
  public static class InnerContentHandler implements Route {
    @Override
    public String handle(Request req, Response res) {
      try {
        return WikiPage.fromName(req.params(":name"))
            .getInnerContent(linkFinder);
      } catch (IOException | IllegalArgumentException e) {
        e.printStackTrace();
        return SparkServer.reqError(e.getMessage());
      }
    }
  }

  /**
   * Handler for getting the outgoing links of a wiki page.
   *
   * @author mcisler
   *
   */
  public static class LinksHandler implements Route {
    @Override
    public String handle(Request req, Response res) {
      try {
        // TODO
        return WikiInterpreter.GSON.toJson(
            linkFinder.linkedPages(WikiPage.fromName(req.params(":name"))));
      } catch (IOException | IllegalArgumentException e) {
        e.printStackTrace();
        return SparkServer.reqError(e.getMessage());
      }
    }
  }
}
