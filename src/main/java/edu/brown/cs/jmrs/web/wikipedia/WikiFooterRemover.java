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
  // pre-define for speed
  private static final String MW_CONTENT_TEXT_SEE_ALSO_SELECT     =
      "#mw-content-text > *:has(#mw-content-text > * > #See_also)";
  private static final String MW_CONTENT_TEXT_EXTERN_LINKS_SELECT =
      "#mw-content-text > *:has(#mw-content-text > * > #External_links)";

  private static final String MW_PARSER_OUTPUT_SEE_ALSO_SELECT     =
      ".mw-parser-output > *:has(.mw-parser-output > * > #See_also)";
  private static final String MW_PARSER_OUTPUT_EXTERN_LINKS_SELECT =
      ".mw-parser-output > *:has(.mw-parser-output > * > #External_links)";

  @Override
  public Element format(Element input) {

    boolean usesParserOutput = input.select(".mw-parser-output").size() != 0;

    // remove everything after "See Also" (inclusive)
    Elements seeAlso =
        input.select(usesParserOutput ? MW_PARSER_OUTPUT_SEE_ALSO_SELECT
            : MW_CONTENT_TEXT_SEE_ALSO_SELECT);
    Elements afterSeeAlso = seeAlso.nextAll();
    if (afterSeeAlso.size() > 0) {
      afterSeeAlso.remove();
    }
    seeAlso.remove();
    // remove everything after "External Links" (inclusive)
    Elements externalLinks =
        input.select(usesParserOutput ? MW_PARSER_OUTPUT_EXTERN_LINKS_SELECT
            : MW_CONTENT_TEXT_EXTERN_LINKS_SELECT);
    Elements afterExternalLinks = externalLinks.nextAll();
    if (afterExternalLinks.size() > 0) {
      afterExternalLinks.remove();
    }
    externalLinks.remove();

    // remove others specifically
    input.select(".reflist").remove();
    input.select(".navbox").remove();

    return input;
  }
}
