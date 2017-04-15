package edu.brown.cs.jmrs.web;

import java.io.IOException;
import java.util.Set;

/**
 * A link finder which finds only interal wikipedia links on a page.
 *
 * @author mcisler
 *
 */
public class WikiPageLinkFinder implements LinkFinder<WikiPage> {
  private final LinkFinderMethod<WikiPage> linkFinder;

  /**
   * Constucts a WikiPageLinkFinder.
   */
  public WikiPageLinkFinder() {
    this.linkFinder =
        new LinkFinderMethod<WikiPage>().select("#mw-content-text a[href]")
            .factory(url -> new WikiPage(url, this));
  }

  @Override
  public Set<String> links(WikiPage page) throws IOException {
    return linkFinder.filter(page::isChildWikipediaArticle).links(page);
  }

  @Override
  public Set<WikiPage> linkedPages(WikiPage page) throws IOException {
    return linkFinder.filter(page::isChildWikipediaArticle).linkedPages(page);
  }
}
