package edu.brown.cs.jmrs.wikispeedia.comms;

import java.io.IOException;

import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.ui.SparkHandlers;
import edu.brown.cs.jmrs.ui.SparkServer;
import edu.brown.cs.jmrs.web.ContentFormatter;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.wikispeedia.GameGenerator;
import edu.brown.cs.jmrs.wikispeedia.WikiLobby;
import spark.QueryParamsMap;
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
public class WikiPageHandlers implements SparkHandlers {
  private static LinkFinder<WikiPage>       linkFinder       =
      WikiLobby.DEFAULT_LINK_FINDER;
  private static ContentFormatter<WikiPage> contentFormatter =
      WikiLobby.DEFAULT_CONTENT_FORMATTER;

  @Override
  public void registerHandlers(FreeMarkerEngine freeMarker) {
    Spark.get("/wiki/:name", new InnerContentHandler());
    Spark.get("/wiki/links/:name", new LinksHandler());
    Spark.get("/random", new GenerateHandler());

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
        return contentFormatter.stringFormat(
            WikiPage.fromName(req.params(":name")).linksMatching(linkFinder));
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
        return Main.GSON.toJson(
            linkFinder.linkedPages(WikiPage.fromName(req.params(":name"))));
      } catch (IOException | IllegalArgumentException e) {
        e.printStackTrace();
        return SparkServer.reqError(e.getMessage());
      }
    }
  }

  /**
   * Handler generating a random wikipage.
   *
   * @author mcisler
   *
   */
  public static class GenerateHandler implements Route {
    @Override
    public String handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();

      try {
        if (qm.hasKey("obscurity")) {
          WikiPage generatedPage =
              GameGenerator
                  .pageWithObscurity(Double.parseDouble(qm.value("obscurity")));

          return generatedPage.content() + String.format("<h1>links: %d</h1>",
              linkFinder.links(generatedPage).size());
        } else {
          return GameGenerator.getRandomPage().content();
        }
      } catch (IOException | IllegalArgumentException e) {
        e.printStackTrace();
        return SparkServer.reqError(e.getMessage());
      }
    }
  }
}
