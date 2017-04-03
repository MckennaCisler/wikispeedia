package edu.brown.cs.jmrs.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;

/**
 * A class representing a webpage and the required functions of a web page.
 *
 * @author mcisler
 *
 */
public class Page {
  private final String url;
  private Document parsed;

  /**
   * @param url
   *          The url of the page to be constructed.
   */
  public Page(String url) {
    this.url = url;
    this.parsed = null;
  }

  /**
   * @return The URL of this page.
   */
  public String url() {
    return url;
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
  public List<Page> links() throws IOException {
    return links("", u -> true);
  }

  /**
   * @param selector
   *          A CSS selector to add onto normal link selection.
   * @return All pages linked to by this page whose elements match the given
   *         selector.
   * @throws IOException
   *           If the page could not be reached.
   */
  public List<Page> links(String selector) throws IOException {
    return links(selector, u -> true);
  }

  /**
   * @param pred
   *          The predicate to filter links by.
   * @return All pages linked to by this page that satisfy the given predicate.
   * @throws IOException
   *           If the page could not be reached.
   */
  public List<Page> links(Predicate<String> pred) throws IOException {
    return links("", pred);
  }

  /**
   * @param selector
   *          A CSS selector to add onto normal link selection.
   * @param pred
   *          The predicate to filter links by.
   * @return All pages linked to by this page whose elements match the given
   *         selector and satisfy the given predicate.
   * @throws IOException
   *           If the page could not be reached.
   */
  public List<Page> links(String selector, Predicate<String> pred)
      throws IOException {
    Elements links = parsedContent().select("a[href]" + selector);

    List<Page> pages = new ArrayList<>(links.size());
    for (Element el : links) {
      String link = el.attr("abs:href");
      if (pred.apply(link)) {
        pages.add(new Page(link));
      }
    }
    return pages;
  }
}
