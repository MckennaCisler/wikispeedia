package edu.brown.cs.jmrs.web;

import java.io.IOException;
import java.util.Set;

import edu.brown.cs.jmrs.ui.JavaUtils;

/**
 * An extension of a page that is designed to handle a wikipedia page in
 * particular.
 *
 * @author mcisler
 *
 */
public class WikiPage extends Page {

  // TODO: Optimize with Pattern.compile() among others :
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

  /***************************************************************************/
  /* Link finding helpers and methods */
  /***************************************************************************/

  /**
   * @param url
   *          The url to test.
   * @return Whether url is under the Wikipedia.org domain.
   */
  public boolean isWikipediaUrl(String url) {
    return cleanUrl(url).matches(WIKIPEDIA_DOMAIN_REGEX);
  }

  /**
   * @param url
   *          The url to test.
   * @return Whether url is a Wikipedia article page.
   */
  public boolean isWikipediaArticle(String url) {
    return url.matches(WIKIPEDIA_DOMAIN_REGEX + "\\/wiki\\/.*")
        // ensure there aren't things of the type "Wikipedia:*" or "File:*"
        && url.indexOf(':', 6) == -1; // 5 = length of 'https:"
  }

  private boolean isChildWikipediaArticle(String url) {
    return isWikipediaArticle(url) && url != super.url();
  }

  /**
   * @return All Wikipedia articles linked to in this article.
   * @throws IOException
   *           If the article could not be read.
   */
  public Set<WikiPage> wikiPageLinks() throws IOException {
    return wikiLinks("#bodyContent");
  }

  /**
   * @return All Wikipedia articles linked to in this article.
   * @throws IOException
   *           If the article could not be read.
   */
  public Set<WikiPage> wikiBodyLinks() throws IOException {
    return wikiLinks("#mw-content-text"); // TODO:
  }

  /**
   * @param parentId
   *          The id of the Wikipedia parent element to find child article links
   *          of.
   * @return All Wikipedia articles linked to in this article.
   * @throws IOException
   *           If the article could not be read.
   */
  private Set<WikiPage> wikiLinks(String parentId) throws IOException {
    return JavaUtils.map(links(String.format("%s a[href]", parentId),
        this::isChildWikipediaArticle), p -> new WikiPage(p.url()));
  }

  public static void main(String[] args) {
    WikiPage start = new WikiPage(WIKIPEDIA_ARTICLE_PREFIX + "Cat");
    try {
      System.out.println(start.getTitle());
      System.out.println(start.getBlurb());
    } catch (IOException e) {
      e.printStackTrace();
    }

    printLinks(start, 1);
  }

  private static void printLinks(WikiPage start, int depth) {
    if (depth == 0) {
      return;
    }

    try {
      Set<WikiPage> links = start.wikiBodyLinks();

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
