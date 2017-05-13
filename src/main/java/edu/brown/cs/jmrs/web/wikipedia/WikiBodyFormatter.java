package edu.brown.cs.jmrs.web.wikipedia;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    Elements possibleRoot = input.select("#mw-content-text");

    // edge case
    if (possibleRoot.size() == 0) {
      possibleRoot = input.select(".mw-parser-output");
    }

    Element root = possibleRoot.first().clone();

    // remove geography tags
    root.select(".geography").remove();

    // and coordinates tags
    root.select("#coordinates").remove();

    return root;
  }
}
