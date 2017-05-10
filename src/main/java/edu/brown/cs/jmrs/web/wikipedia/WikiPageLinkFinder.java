package edu.brown.cs.jmrs.web.wikipedia;

import java.io.IOException;
import java.util.Set;
import java.util.function.Predicate;

import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.web.ContentFormatter;
import edu.brown.cs.jmrs.web.Link;
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
  private final Predicate<String>          filterMethod;

  /**
   * An enum of possible link filters ("invalidators") to apply to this
   * particular LinkFinder.
   *
   * @author mcisler
   *
   */
  public enum Filter {
    DISAMBIGUATION((url) -> url.contains("(disambiguation)")), //
    NON_ENGLISH_WIKIPEDIA((url) -> !url.contains("en.wikipedia.org"));

    // a method to IGNORE links by (if it's true, the link is filtered out)
    private Predicate<String> method;

    Filter(Predicate<String> method) {
      this.method = method;
    }

    /**
     * @return True if url should be filtered out, false otherwise.
     */
    boolean test(String url) {
      return method.test(url);
    }
  }

  /**
   * Constucts a WikiPageLinkFinder.
   *
   * @param formatter
   *          Formatter to use before.
   * @param filters
   *          A series of filters to ignore links by.
   */
  public WikiPageLinkFinder(ContentFormatter<WikiPage> formatter,
      Filter... filters) {
    this.linkFinderMethod =
        new LinkFinderMethod<WikiPage>().select("#mw-content-text a[href]")
            .factory(url -> new WikiPage(url, Main.WIKI_PAGE_DOC_CACHE))
            .formatter(formatter);
    this.filterMethod = getFilterMethod(filters);
  }

  @Override
  public Set<String> links(WikiPage page) throws IOException {
    return linkFinderMethod.filter(
        (url) -> page.isChildWikipediaArticle(url) && filterMethod.test(url))
        .links(page);
  }

  @Override
  public Set<WikiPage> linkedPages(WikiPage page) throws IOException {
    return linkFinderMethod.filter(
        (url) -> page.isChildWikipediaArticle(url) && filterMethod.test(url))
        .linkedPages(page);
  }

  /**
   * @return A filter method predicate which is true only when all filters are
   *         false.
   */
  private Predicate<String> getFilterMethod(Filter... filters) {
    return (url) -> {
      for (Filter filter : filters) {
        // if this filter says we should filter it, don't take this url
        if (filter.test(url)) {
          return false;
        }
      }
      return true;
    };
  }

  @Override
  public Number edgeValue(Link edge) {
    // just return generic value
    return 1;
  }
}
