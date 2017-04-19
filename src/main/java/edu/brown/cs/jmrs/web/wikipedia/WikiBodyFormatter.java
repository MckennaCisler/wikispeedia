package edu.brown.cs.jmrs.web.wikipedia;

import org.jsoup.nodes.Element;

import edu.brown.cs.jmrs.web.ContentFormatter;

/**
 * A basic ContentFormatter which extracts the body from a Wikipedia page.
 * Should be the earliest WikiFormatter in a chain.
 *
 * @author mcisler
 *
 */
public class WikiBodyFormatter implements ContentFormatter<WikiPage> {

  @Override
  public Element format(Element input) {
    // TODO : More efficent?
    Element content = input.select("#content").first().clone();

    // remove everything before the title
    content.select("#content > #firstHeading").prevAll().remove();

    // remove everything after mw-content-text
    content.select("#content > #bodyContent > #mw-content-text").nextAll()
        .remove();

    return content;
  }
}
