package edu.brown.cs.jmrs.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Predicate;

/**
 * An interface for a method to find outgoing links of a Page.
 *
 * @author mcisler
 *
 * @param <P>
 *          The page implementation to take in and use to create for links.
 */
public interface LinkFinder<P extends Page> {
  /**
   * Finds the outgoing links from the given page.
   *
   * @param page
   *          The root page.
   * @return A list of urls of each outgoing link.
   * @throws IOException
   *           If the page could not be reached.
   */
  Set<String> links(P page) throws IOException;

  /**
   * Finds the outgoing pages from the given page.
   *
   * @param page
   *          The root page.
   * @return A list of pages corresponding to links.
   *
   * @throws IOException
   *           If the page could not be reached.
   */
  Set<P> linkedPages(P page) throws IOException;

}

/**
 * A Builder to build a LinkFinderMethod. Provides several common utilities for
 * doing so.
 *
 * @author mcisler
 *
 * @param <P>
 *          The page implementation to create for links in linkedPages().
 */
class LinkFinderMethod<P extends Page> implements LinkFinder<P> {
  // optional
  private String selector = "a[href]";
  private Predicate<String> pred = u -> true;

  private Function<String, P> factory = url -> (P) new Page(url);

  /**
   * Construct a default LinkFinderMethod to find links from page.
   *
   * @param page
   *          The page to find links from.
   */
  LinkFinderMethod() {
  }

  /**
   * @param selector
   *          A CSS selector to use to select links (elements must have href
   *          attribute).
   * @return A LinkFinderMethod using this selector.
   */
  LinkFinderMethod<P> select(String s) {
    this.selector = s;
    return this;
  }

  /**
   * @param pred
   *          The predicate to filter links by, using their strings.
   * @return A LinkFinderMethod using this predicate.
   */
  LinkFinderMethod<P> filter(Predicate<String> p) {
    this.pred = p;
    return this;
  }

  /**
   * @param f
   *          A function to create a type of page given it's url.
   * @return A LinkFinderMethod using this factory.
   */
  LinkFinderMethod<P> factory(Function<String, P> f) {
    this.factory = f;
    return this;
  }

  @Override
  public Set<String> links(Page page) throws IOException {
    Elements links = page.parsedContent().select(selector);

    Set<String> urls = new HashSet<>(links.size());
    for (Element el : links) {
      String link = el.attr("abs:href");
      if (pred.apply(link)) {
        urls.add(link);
      }
    }
    return urls;
  }

  /**
   * Finds the outgoing pages from the given page using the factory optionally
   * set in this LinkFinderMethod.
   *
   * @param page
   *          The root page.
   * @return A list of pages corresponding to links.
   *
   * @throws IOException
   *           If the page could not be reached.
   */
  @Override
  public Set<P> linkedPages(Page page) throws IOException {
    Set<String> urls = links(page);
    Set<P> output = new HashSet<>(urls.size());
    urls.forEach(url -> output.add(factory.apply(url)));
    return output;
  }
}
