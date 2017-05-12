package edu.brown.cs.jmrs.web.wikipedia;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.brown.cs.jmrs.web.ContentFormatter;

/**
 * ContentFormatter for WikiPages that removes the footer of a page (See Also,
 * etc.).
 *
 * @author mcisler
 *
 */
public class WikiFooterRemover implements ContentFormatter<WikiPage> {

  @Override
  public Element format(Element input) {
    // remove everything after "See Also" (inclusive)
    Elements seeAlso =
        input.select(
            "#mw-content-text > *:has(#mw-content-text > * > #See_also)");
    Elements afterSeeAlso = seeAlso.nextAll();
    if (afterSeeAlso.size() > 0) {
      afterSeeAlso.remove();
    }
    seeAlso.remove();
    // remove everything after "External Links" (inclusive)
    Elements externalLinks =
        input.select(
            "#mw-content-text > *:has(#mw-content-text > * > #External_links)");
    Elements afterExternalLinks = externalLinks.nextAll();
    if (afterExternalLinks.size() > 0) {
      afterExternalLinks.remove();
    }
    externalLinks.remove();

    // remove others specifically
    input.select(".reflist").remove();
    input.select(".navbox").remove();

    assert input
        .select("#mw-content-text > *:has(#mw-content-text > * > #See_also)")
        .size() == 0;
    return input;
  }
}
