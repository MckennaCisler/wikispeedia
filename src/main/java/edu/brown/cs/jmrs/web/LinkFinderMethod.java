package edu.brown.cs.jmrs.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Predicate;

/**
 * A Builder to build a LinkFinderMethod. Provides several common utilities for
 * doing so.
 *
 * @author mcisler
 *
 * @param <P>
 *          The page implementation to create for links in linkedPages().
 */
public class LinkFinderMethod<P extends Page> implements LinkFinder<P> {

  // optional
  private String            selector = "a[href]";
  private Predicate<String> pred     = u -> true;

  private Function<String, P> factory = url -> (P) new Page(url);

  private ContentFormatter<P> formatter = new ContentFormatter<P>() {
    @Override
    public Element format(Element input) {
      return input;
    }
  };

  /**
   * @param s
   *          A CSS selector to use to select links (elements must have href
   *          attribute).
   * @return A LinkFinderMethod using this selector.
   */
  public LinkFinderMethod<P> select(String s) {
    this.selector = s;
    return this;
  }

  /**
   * @param p
   *          The predicate to filter links by, using their strings.
   * @return A LinkFinderMethod using this predicate.
   */
  public LinkFinderMethod<P> filter(Predicate<String> p) {
    this.pred = p;
    return this;
  }

  /**
   * @param f
   *          A function to create a type of page given it's url.
   * @return A LinkFinderMethod using this factory.
   */
  public LinkFinderMethod<P> factory(Function<String, P> f) {
    this.factory = f;
    return this;
  }

  /**
   * @param fm
   *          A ContentFormater to apply on the page before finding the links.
   * @return A LinkFinderMethod using this formatter.
   */
  public LinkFinderMethod<P> formatter(ContentFormatter<P> fm) {
    this.formatter = fm;
    return this;
  }

  @Override
  public Set<String> links(P page) throws IOException {
    Elements links = formatter.format(page).select(selector);

    Set<String> urls = new HashSet<>(links.size());
    for (Element el : links) {
      // use absolute link in case there are external links which may have a
      // wikipedia-like suffix.
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
  public Set<P> linkedPages(P page) throws IOException {
    Set<String> urls = links(page);
    Set<P> output = new HashSet<>(urls.size());
    urls.forEach(url -> output.add(factory.apply(url)));
    return output;
  }
}
