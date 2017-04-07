package edu.brown.cs.jmrs.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;

/**
 * A class representing a webpage and the required functions of a web page.
 *
 * @author mcisler
 *
 */
public class Page {
  // Useful regexs
  public static final String HTTP_REGEX = "^https?:\\/\\/";

  private String url;
  private Document parsed;

  /**
   * @param url
   *          The url of the page to be constructed. May change internally if
   *          this url redirects.
   */
  public Page(String url) {
    this.url = cleanUrl(url);
    this.parsed = null;
  }

  /**
   * @return The URL this page was instantiated with.
   */
  public String url() {
    return url;
  }

  /**
   * @return Rhe final url of the page after it was accessed (and potentially
   *         redirected). May not equal url().
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String finalUrl() throws IOException {
    return cleanUrl(parsedContent().location());
  }

  /**
   * Reads in the content of the page, either using already-parsed html or by a
   * direct download of the content.
   *
   * @return The raw content of the page.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String content() throws IOException {
    if (parsed != null) {
      return parsed.outerHtml();
    } else {
      try (BufferedReader in =
          new BufferedReader(new InputStreamReader(new URL(url).openStream(),
              Charsets.UTF_8))) {
        StringBuilder content = new StringBuilder();

        String line = "";
        do {
          content.append(line);
          line = in.readLine();
        } while (line != null);

        return content.toString();
      }
    }
  }

  /**
   * @return The parsed content of the page.
   * @throws IOException
   *           If the page could not be reached.
   */
  public Document parsedContent() throws IOException {
    if (parsed == null) {
      parsed = Jsoup.connect(url).get();
    }
    return parsed;
  }

  /**
   * @return All pages linked to by this page.
   * @throws IOException
   *           If the page could not be reached.
   */
  public Set<Page> links() throws IOException {
    return links("a[href]", u -> true);
  }

  /**
   * @param pred
   *          The predicate to filter links by, using their strings.
   * @return All pages linked to by this page that satisfy the given predicate.
   * @throws IOException
   *           If the page could not be reached.
   */
  public Set<Page> links(Predicate<String> pred) throws IOException {
    return links("a[href]", pred);
  }

  /**
   * @param selector
   *          A CSS selector to add onto normal link selection.
   * @return All pages linked to by this page whose elements match the given
   *         selector.
   * @throws IOException
   *           If the page could not be reached.
   */
  protected Set<Page> links(String selector) throws IOException {
    return links(selector, u -> true);
  }

  /**
   * @param selector
   *          A CSS selector to use to select links (elements must have href
   *          attribute).
   * @param pred
   *          The predicate to filter links by, using their strings.
   * @return All pages linked to by this page whose elements match the given
   *         selector and satisfy the given predicate.
   * @throws IOException
   *           If the page could not be reached.
   */
  protected Set<Page> links(String selector, Predicate<String> pred)
      throws IOException {
    Elements links = parsedContent().select(selector);

    Set<Page> pages = new HashSet<>(links.size());
    for (Element el : links) {
      String link = el.attr("abs:href");
      if (pred.apply(link)) {
        pages.add(new Page(link));
      }
    }
    return pages;
  }

  /**
   * Cleans the given url of irrelevant or non-supported aspects. Note that
   * query strings are not considered to be independent urls.
   *
   * Based loosely on notes here:
   * http://www.searchtools.com/robots/robot-checklist.html
   * https://www.talisman.org/~erlkonig/misc/
   * lunatech^what-every-webdev-must-know-about-url-encoding
   */
  protected String cleanUrl(String u) {
    return u.replaceAll("\\#.*$", "") // remove anchor tags
        .replaceAll("\\?.*$", "") // remove query strings (after tags)
        .replaceAll("'|\"", ""); // remove quotes
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(url);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    Page other = (Page) obj;
    return url.equals(other.url);
  }
}
