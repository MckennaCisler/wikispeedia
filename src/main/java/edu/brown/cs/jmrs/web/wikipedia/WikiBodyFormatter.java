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
    Element content = input.select("#bodyContent").first().clone();

    // remove everything after mw-content-text
    content.select("#bodyContent > #mw-content-text").nextAll().remove();

    return content;
  }
}
