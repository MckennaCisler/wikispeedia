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
            "#content > #bodyContent > #mw-content-text > *:has(#See_also)");
    seeAlso.nextAll().remove();
    seeAlso.remove();
    return seeAlso.first(); // there should only be one.
  }
}
