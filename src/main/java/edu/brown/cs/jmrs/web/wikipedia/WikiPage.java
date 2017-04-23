package edu.brown.cs.jmrs.web.wikipedia;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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

  // TODO Optimize with Pattern.compile() among others :
  // http://www.javaworld.com/article/2077757/core-java/optimizing-regular-expressions-in-java.html?page=2
  public static final String WIKIPEDIA_ARTICLE_PREFIX =
      "https://en.wikipedia.org/wiki/";

  // Common regexes
  public static final Pattern WIKIPEDIA_DOMAIN_REGEX =
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
    String cleanedId = cleanUrl(identifier);

    // remove a VERY last slash
    if (cleanedId.charAt(cleanedId.length() - 1) == '/') {
      cleanedId = cleanedId.substring(0, cleanedId.length() - 2);
    }

    int lastSlash = cleanedId.lastIndexOf('/');

    // extract title and add it onto full link to be safe
    if (lastSlash != -1) {
      return WikiPage.fromName(cleanedId.substring(lastSlash + 1));
    } else {
      // already just a title
      return WikiPage.fromName(cleanedId);
    }
  }

  /**
   * @return The name of this Wikipedia page as indicated by it's url.
   */
  public String getName() {
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
   * @return The title of this Wikipedia page.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String getBlurb() throws IOException {
    return parsedContentOriginal().select("#mw-content-text > p").first().text()
        .replaceAll("\\[\\d+\\]", "");
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
    return isWikipediaArticle(url) && url != super.url();
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

  /**
   * Main for general testing.
   *
   * @param args
   *          The arguments of main, not used. (I'm really only doing this to
   *          appease checkstyle. This comment has no point otherwise).
   */
  public static void main(String[] args) {
    WikiPage start = new WikiPage(WIKIPEDIA_ARTICLE_PREFIX + "Cat");
    try {
      System.out.println(start.getTitle());
      System.out.println(start.getBlurb());
      start.parsedContent();
      // System.out.println(start.getInnerContent());
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }

    printLinks(start, 100);
  }

  private static void printLinks(WikiPage start, int depth) {
    if (depth == 0) {
      return;
    }

    try {
      Set<WikiPage> links = new WikiPageLinkFinder().linkedPages(start);

      for (WikiPage page : links) {
        System.out.println(start.url() + " -> " + page.url());
        // System.out.println(page.getTitle());
        // System.out.println(page.getBlurb());
        printLinks(page, depth - 1);
      }
      System.out.println(String.format("\t%d links found under %s",
          links.size(), start.url()));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
  }
}
