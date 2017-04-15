package edu.brown.cs.jmrs.web;

import java.io.IOException;

import edu.brown.cs.jmrs.io.JsonSerializable;
import edu.brown.cs.jmrs.main.SparkHandlers;
import edu.brown.cs.jmrs.main.SparkServer;
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
        return WikiPage.fromName(req.params(":name")).getInnerContent();
      } catch (IOException | IllegalArgumentException e) {
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
        return JsonSerializable.toJson(
            linkFinder.linkedPages(WikiPage.fromName(req.params(":name"))),
            (wp) -> {
              try {
                return wp.toJsonFull();
              } catch (IOException e) {
                return "";
              }
            });
      } catch (IOException | IllegalArgumentException e) {
        return SparkServer.reqError(e.getMessage());
      }
    }
  }
}
