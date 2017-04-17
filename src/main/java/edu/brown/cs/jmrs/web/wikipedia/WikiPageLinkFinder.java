package edu.brown.cs.jmrs.web.wikipedia;

import java.io.IOException;
import java.util.Set;

import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.LinkFinderMethod;

/**
 * A link finder which finds only interal wikipedia links on a page.
 *
 * @author mcisler
 *
 */
public class WikiPageLinkFinder implements LinkFinder<WikiPage> {
  private final LinkFinderMethod<WikiPage> linkFinderMethod;

  /**
   * Constucts a WikiPageLinkFinder.
   */
  public WikiPageLinkFinder() {
    this.linkFinderMethod =
        new LinkFinderMethod<WikiPage>().select("#mw-content-text a[href]")
            .factory(url -> new WikiPage(url));
  }

  @Override
  public Set<String> links(WikiPage page) throws IOException {
    return linkFinderMethod.filter(page::isChildWikipediaArticle).links(page);
  }

  @Override
  public Set<WikiPage> linkedPages(WikiPage page) throws IOException {
    return linkFinderMethod.filter(page::isChildWikipediaArticle)
        .linkedPages(page);
  }
}
