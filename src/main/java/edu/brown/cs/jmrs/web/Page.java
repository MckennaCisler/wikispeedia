package edu.brown.cs.jmrs.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;

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
   * Creates a page with the default (all links) page finder.
   *
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
   * Cleans the given url of irrelevant or non-supported aspects. Note that
   * query strings are not considered to be independent urls.
   *
   * Based loosely on notes here:
   * http://www.searchtools.com/robots/robot-checklist.html
   * https://www.talisman.org/~erlkonig/misc/
   * lunatech^what-every-webdev-must-know-about-url-encoding
   */
  protected static String cleanUrl(String u) {
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
