package edu.brown.cs.jmrs.web.wikipedia;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.brown.cs.jmrs.web.ContentFormatter;
import edu.brown.cs.jmrs.web.LinkFinder;
import edu.brown.cs.jmrs.web.Page;

/**
 * An extension of a page that is designed to handle a wikipedia page in
 * particular.
 *
 * @author mcisler
 *
 */
public class WikiPage extends Page {

  public static final String WIKIPEDIA_ARTICLE_PREFIX =
      "https://en.wikipedia.org/wiki/";

  // Common regexes
  public static final Pattern WIKIPEDIA_DOMAIN_REGEX  =
      Pattern.compile(HTTP_REGEX + ".*?\\.wikipedia\\.org");
  public static final Pattern WIKIPEDIA_ARTICLE_REGEX =
      Pattern.compile(WIKIPEDIA_DOMAIN_REGEX.pattern() + "\\/wiki\\/.*");

  /**
   * @param url
   *          The url of the wikipedia page to be constructed.
   */
  public WikiPage(String url) {
    super(url);
    assert isWikipediaArticle(url);
  }

  /**
   * Creates a page based on the given URL. Content is downloaded upon request,
   * and stored in the included cache.
   *
   * @param url
   *          The url of the wikipedia page to be constructed.
   * @param docCache
   *          The cache from url to parsed HTML document to be used to store and
   *          retrieve the internal HTML.
   */
  public WikiPage(String url, LoadingCache<String, Document> docCache) {
    super(url, docCache);
    assert isWikipediaArticle(url);
  }

  /**
   * Constructs a Wikipage from the given page string.
   *
   * @param name
   *          The String page to convert to a Wikipedia URL. If there are
   *          spaces, they will be replaced with underscores.
   *
   * @return A new Wikipage constructed with the url using the given page.
   */
  public static WikiPage fromName(String name) {
    // it will likely be escaped, but replace spaces otherwise.
    return new WikiPage(WIKIPEDIA_ARTICLE_PREFIX + name.replaceAll("\\s", "_"));
  }

  /**
   * Constructs a Wikipage from the given page string,using a particular cache
   * for getting content.
   *
   * @param name
   *          The String page to convert to a Wikipedia URL. If there are
   *          spaces, they will be replaced with underscores.
   * @param docCache
   *          The cache from url to parsed HTML document to be used to store and
   *          retrieve the internal HTML.
   *
   * @return A new Wikipage constructed with the url using the given page.
   */
  public static WikiPage fromName(String name,
      LoadingCache<String, Document> docCache) {
    // it will likely be escaped, but replace spaces otherwise.
    return new WikiPage(WIKIPEDIA_ARTICLE_PREFIX + name.replaceAll("\\s", "_"),
        docCache);
  }

  /**
   * Constructs a Wikipage from the given string of some type and attempts to
   * create the appropriate page.
   *
   * @param identifier
   *          The String page to convert to a Wikipedia URL. Can be either a
   *          full wiki url, a relative wiki url, or a page name.
   *
   * @return A new Wikipage constructed with the url using the given name.
   */
  public static WikiPage fromAny(String identifier) {
    return fromAny(identifier, null);
  }

  /**
   * Constructs a Wikipage from the given string of some type and attempts to
   * create the appropriate page. Also takes in a cache for storing internal
   * data.
   *
   * @param identifier
   *          The String page to convert to a Wikipedia URL. Can be either a
   *          full wiki url, a relative wiki url, or a page name.
   * @param docCache
   *          The cache from url to parsed HTML document to be used to store and
   *          retrieve the internal HTML.
   *
   * @return A new Wikipage constructed with the url using the given name.
   */
  public static WikiPage fromAny(String identifier,
      LoadingCache<String, Document> docCache) {
    String cleanedId = cleanUrl(identifier);

    // remove a VERY last slash
    if (cleanedId.charAt(cleanedId.length() - 1) == '/') {
      cleanedId = cleanedId.substring(0, cleanedId.length() - 2);
    }

    int lastSlash = cleanedId.lastIndexOf('/');

    // extract title and add it onto full link to be safe
    if (lastSlash != -1) {
      return WikiPage.fromName(cleanedId.substring(lastSlash + 1), docCache);
    } else {
      // already just a title
      return WikiPage.fromName(cleanedId, docCache);
    }
  }

  /**
   * @return The name of this Wikipedia page as indicated by it's url.
   */
  public String getName() {
    assert url().contains("/");
    int lastSlash = url().lastIndexOf('/');
    if (lastSlash != -1 && lastSlash + 1 < url().length()) {
      return url().substring(lastSlash + 1);
    } else {
      return url();
    }
  }

  /**
   * @return The title of this Wikipedia page.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String getTitle() throws IOException {
    return parsedContentOriginal().select("#firstHeading").text();
  }

  /**
   * @return The blurb of this Wikipedia page.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String getBlurb() throws IOException {
    String para;
    Elements paragraphs =
        parsedContentOriginal().select("#mw-content-text > p");
    int i = 0;
    do {
      para = paragraphs.get(i++).text();
    } while (para.equals("") && i < paragraphs.size());
    return para;
  }

  /**
   * @return The blurb of this Wikipedia page formatted with formatter.
   * @param formatter
   *          The ContentFormatter to use in formatting this page.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String getFormattedBlurb(ContentFormatter<WikiPage> formatter)
      throws IOException {
    String para;
    Elements paragraphs = formatter.format(this).select("#mw-content-text > p");
    int i = 0;
    do {
      para = paragraphs.get(i++).text();
    } while (para.equals("") && i < paragraphs.size());
    return para;
  }

  /**
   * @return The parsedContent() of the Wikipedia page, but with any links NOT
   *         matched by lf replaced with just their plaintext. Returns Element
   *         so that can be input into ContentReformatters.
   * @param lf
   *          The LinkFinder to use to limit the links on the page.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public Element linksMatching(LinkFinder<WikiPage> lf) throws IOException {
    Set<String> allowableLinks = lf.links(this);
    Element content = parsedContent(); // copy
    // note: the speed of this is dependent on allowableLinks being a HashSet
    for (Element link : content.select("a[href]")) {
      if (!allowableLinks.contains(link.attr("abs:href"))) {
        // replace the element with only it's inner html
        String html = link.html();
        link.before(html);
        link.remove();
      }
    }
    return content;
  }

  /***************************************************************************/
  /* Link finding helpers and methods */
  /***************************************************************************/

  /**
   * @param url
   *          The url to test.
   * @return Whether url is under the Wikipedia.org domain.
   */
  public static boolean isWikipediaUrl(String url) {
    return WIKIPEDIA_DOMAIN_REGEX.matcher(cleanUrl(url)).matches();
  }

  /**
   * @param url
   *          The url to test.
   * @return Whether url is a Wikipedia article page.
   */
  public static boolean isWikipediaArticle(String url) {
    return WIKIPEDIA_ARTICLE_REGEX.matcher(url).matches()
        // ensure there aren't things of the type "Wikipedia:*" or "File:*"
        && url.indexOf(':', 6) == -1 // 5 = length of 'https:"
        && !url.contains("#cite_note");
  }

  /**
   * @param url
   *          The url of this page.
   * @return Whether url is a Wikipedia article page other than this one.
   */
  public boolean isChildWikipediaArticle(String url) {
    return isWikipediaArticle(url) && !url.equals(super.url());
  }

  @Override
  public String toString() {
    return url();
  }

  @Override
  public boolean equalsAfterRedirect(Page page) throws IOException {
    if (this == page) {
      return true;
    }
    if (page == null) {
      return false;
    }
    assert page instanceof WikiPage;
    return getTitle().equals(((WikiPage) page).getTitle());
  }

  /**
   * Custom serializer for use with GSON.
   *
   * @author mcisler
   *
   */
  public static class Serializer implements JsonSerializer<WikiPage> {

    @Override
    public JsonElement serialize(WikiPage src, Type typeOfSrc,
        JsonSerializationContext context) {
      JsonObject root = new JsonObject();
      root.addProperty("url", src.url());
      root.addProperty("name", src.getName());
      // all we can do without actually getting it
      return root;
    }
  }
}
