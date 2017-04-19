package edu.brown.cs.jmrs.web.wikipedia;

import org.jsoup.nodes.Element;

import edu.brown.cs.jmrs.web.ContentFormatter;

/**
 * A ContentFormatter which removes various aspects of a WikiPage which are not
 * necessary for display.
 *
 * @author mcisler
 *
 */
public class WikiAnnotationRemover implements ContentFormatter<WikiPage> {

  @Override
  public Element format(Element input) {
    // remove all references
    input.select("sup.reference").remove();

    // remove all "[edit]" tags
    input.select(".mw-editsection").remove();

    return input;
  }
}
