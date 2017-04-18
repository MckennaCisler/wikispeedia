package edu.brown.cs.jmrs.web.wikipedia;

import java.io.IOException;
import java.util.Set;

import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableMap;

import edu.brown.cs.jmrs.io.JsonSerializable;
import edu.brown.cs.jmrs.web.Page;

/**
 * An extension of a page that is designed to handle a wikipedia page in
 * particular.
 *
 * @author mcisler
 *
 */
public class WikiPage extends Page implements JsonSerializable {

  // TODO Optimize with Pattern.compile() among others :
  // http://www.javaworld.com/article/2077757/core-java/optimizing-regular-expressions-in-java.html?page=2
  public static final String WIKIPEDIA_ARTICLE_PREFIX =
      "https://en.wikipedia.org/wiki/";

  // Common regexes
  public static final String WIKIPEDIA_DOMAIN_REGEX =
      HTTP_REGEX + ".*?\\.wikipedia\\.org";

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
    return new WikiPage(WIKIPEDIA_ARTICLE_PREFIX + name.replaceAll("\\s", "_"));
  }

  /**
   * @return The name of this Wikipedia page as indicated by it's url.
   */
  public String getName() {
    return url().substring(url().lastIndexOf('/'));
  }

  /**
   * @return The title of this Wikipedia page.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String getTitle() throws IOException {
    return parsedContent().select("#firstHeading").text();
  }

  /**
   * @return The title of this Wikipedia page.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String getBlurb() throws IOException {
    return parsedContent().select("#mw-content-text > p").first().text()
        .replaceAll("\\[\\d+\\]", "");
  }

  /**
   * @return The article-specific content in the Wikipedia page.
   * @throws IOException
   *           If the page could not be reached or loaded.
   */
  public String getInnerContent() throws IOException {
    // TODO : More efficent?
    Elements content = parsedContent().select("#content").clone();

    // remove everything before the title
    content.select("#content > #firstHeading").prevAll().remove();

    // remove everything after mw-content-text
    content.select("#content > #bodyContent > #mw-content-text").nextAll()
        .remove();

    // remove everything after "See Also" (inclusive)
    Elements seeAlso =
        content.select(
            "#content > #bodyContent > #mw-content-text  > *:has(#See_also)");
    seeAlso.nextAll().remove();
    seeAlso.remove();

    return content.outerHtml();
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
    return cleanUrl(url).matches(WIKIPEDIA_DOMAIN_REGEX);
  }

  /**
   * @param url
   *          The url to test.
   * @return Whether url is a Wikipedia article page.
   */
  public static boolean isWikipediaArticle(String url) {
    return url.matches(WIKIPEDIA_DOMAIN_REGEX + "\\/wiki\\/.*")
        // ensure there aren't things of the type "Wikipedia:*" or "File:*"
        && url.indexOf(':', 6) == -1; // 5 = length of 'https:"
  }

  /**
   * @param url
   *          The url of this page.
   * @return Whether url is a Wikipedia article page other than this one.
   */
  public boolean isChildWikipediaArticle(String url) {
    return isWikipediaArticle(url) && url != super.url();
  }

  @Override
  public String toJson() {
    return toJson(ImmutableMap.of("url", url(), "name", getName()));
  }

  /**
   * @return A more complete Json of this Wikipedia page which requests data
   *         about the page.
   * @throws IOException
   *           If the article could not be read.
   */
  public String toJsonFull() throws IOException {
    return toJson(ImmutableMap.of("url", url(), "name", getName(), "title",
        getTitle(), "blurb", getBlurb()));
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
      start.getInnerContent();
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
