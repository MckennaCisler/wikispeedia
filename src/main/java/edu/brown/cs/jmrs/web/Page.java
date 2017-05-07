package edu.brown.cs.jmrs.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.ExecutionException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import edu.brown.cs.jmrs.collect.graph.Graph.Node;
import edu.brown.cs.jmrs.ui.Main;

/**
 * A class representing a webpage and the required functions of a web page.
 *
 * @author mcisler
 *
 */
public class Page implements Node<Page, Link> {
  // Useful regexs
  // (not compiled because just concatenated to others)
  public static final String HTTP_REGEX = "^https?:\\/\\/";

  private String url;

  // only one of the following will be used
  private Document parsed;
  private LoadingCache<String, Document> docCache;
  private boolean cached; // note this is not universal; it may have been
                          // evicted separately.

  /**
   * Creates a page based on the given URL. Content is downloaded upon request
   * and stored in this object.
   *
   * @param url
   *          The url of the page to be constructed. May change internally if
   *          this url redirects.
   */
  public Page(String url) {
    this(url, null);
  }

  /**
   * Creates a page based on the given URL. Content is downloaded upon request,
   * and stored in the included cache.
   *
   * @param url
   *          The url of the page to be constructed. May change internally if
   *          this url redirects.
   * @param docCache
   *          The cache from url to parsed HTML document to be used to store and
   *          retrieve the internal HTML.
   */
  public Page(String url, LoadingCache<String, Document> docCache) {
    this.url = cleanUrl(url);
    this.parsed = null;
    this.docCache = docCache;
    cached = false;
  }

  /**
   * @return The URL this page was instantiated with.
   */
  public String url() {
    return url;
  }

  @Override
  public String getValue() {
    return url;
  }

  /**
   * @return Rhe final url of the page after it was accessed (and potentially
   *         redirected). May not equal url().
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String finalUrl() throws IOException {
    return cleanUrl(parsedContentOriginal().location());
  }

  /**
   * Reads in the content of the page, either using already-parsed html or by a
   * direct download of the content.
   *
   * Note: does not use the docCache even if defined.
   *
   * @return The raw content of the page.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String content() throws IOException {
    if (cached) {
      // may retrieve if docCache was evicted elsewhere.
      return parsedContentOriginal().outerHtml();
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
    return parsedContentOriginal().clone();
  }

  /**
   * @return The parsed content of the page. Note: for efficiency reasons, this
   *         returns the interior copy of the parsed content, which WILL be
   *         changed if modified outside of this object. Use caution when using.
   * @throws IOException
   *           If the page could not be reached.
   */
  protected Document parsedContentOriginal() throws IOException {
    if (docCache != null) {
      // parsed should not be set if docCache is set
      assert parsed == null;
      try {
        // try to grab from external cache
        Document result = docCache.get(url);
        cached = true;
        return result;
      } catch (ExecutionException e) {

        Main.debugLog(e.getCause());

        // IOExceptions are expected; others are not
        if (e.getCause() instanceof IOException) {
          throw (IOException) e.getCause();
        } else {
          throw new UncheckedExecutionException(e);
        }
      }
    } else if (parsed == null) {
      // just use internal cache
      parsed = Loader.loadStatic(url);
      cached = true;
    }
    return parsed;
  }

  /**
   * @param formatter
   *          The formatter to format parsedContent() by.
   * @return The parsedContent() of this Page, but formatted using formatter.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String formattedContent(ContentFormatter<Page> formatter)
      throws IOException {
    return formatter.stringFormat(this);
  }

  /**
   * Tries to access this page.
   *
   * @return Whether this page can be accessed.
   */
  public boolean accessible() {
    return accessible(false);
  }

  /**
   * Tries to access this page.
   *
   * @param cacheIfSo
   *          Determines whether the page is cached if it IS accessible.
   * @return Whether this page can be accessed.
   */
  public boolean accessible(boolean cacheIfSo) {
    try {
      if (cacheIfSo) {
        cache();
      } else {
        content();
      }
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Attempts to access this Page and cache it's HTML.
   *
   * @return This Page.
   * @throws IOException
   *           If this page cannot be accessed to cache.
   */
  public Page cache() throws IOException {
    parsedContentOriginal();
    return this;
  }

  /**
   * Clears the internal storage of the parsed content of this page.
   */
  public void clearCache() {
    if (docCache != null) {
      docCache.invalidate(url);
    } else {
      parsed = null;
    }
    cached = false;
  }

  /**
   * A basic cache loader for loading a document given a URL, for use in an
   * external docCache.
   *
   * @author mcisler
   *
   */
  public static class Loader extends CacheLoader<String, Document> {
    @Override
    public Document load(String url) throws IOException {
      return loadStatic(url);
    }

    /**
     * Loads a Document from a url.
     *
     * @param url
     *          The url of the page to get parsed HTML from.
     * @return The parsed HTML.
     * @throws IOException
     *           If the page could not be contacted.
     */
    public static Document loadStatic(String url) throws IOException {
      return Jsoup.connect(url).get();
    }
  }

  /**
   * Cleans the given url of irrelevant or non-supported aspects. Note that
   * query strings are not considered to be independent urls.
   *
   * Based loosely on notes here:
   * http://www.searchtools.com/robots/robot-checklist.html
   * https://www.talisman.org/~erlkonig/misc/
   * lunatech^what-every-webdev-must-know-about-url-encoding
   *
   * @param u
   *          The URL to clean
   * @return The cleaned URL.
   */
  protected static String cleanUrl(String u) {
    try {
      return URLDecoder.decode(u, "UTF-8") // unescape escaped entries
          .replaceAll("\\#.*$", "") // remove anchor tags
          .replaceAll("\\?.*$", "") // remove query strings (after tags)
          .replaceAll("\"", ""); // remove quotes (NOT apostrophes)
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("UTF-8 not available", e);
    }
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

  /**
   * An equals method that compares the final (potentially redirected) urls of
   * the pages.
   *
   * @param page
   *          The page to compare to this one.
   * @return Whether this page equals page.
   * @throws IOException
   *           If either page could not be accessed.
   */
  public boolean equalsAfterRedirect(Page page) throws IOException {
    if (this == page) {
      return true;
    }
    if (page == null) {
      return false;
    }
    return finalUrl().equals(page.finalUrl());
  }

  /**
   * @param page
   *          The page to compare to this one.
   * @return Whether this page equals page, trying to check after redirect but
   *         reverting to old otherwise.
   */
  public boolean equalsAfterRedirectSafe(Page page) {
    try {
      return equalsAfterRedirect(page);
    } catch (IOException e) {
      return equals(page);
    }
  }
}
