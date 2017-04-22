package edu.brown.cs.jmrs.web;

import java.io.IOException;

import org.jsoup.nodes.Element;

/**
 * An interface for classes defining a particular way of parsing and formatting
 * a page's innerContent.
 *
 * @author mcisler
 *
 * @param <T>
 *          The subtype of page this ContentFormatter formats.
 *
 */
public interface ContentFormatter<T extends Page> {
  /**
   * Converts a HTML Element into another Element based on the specification in
   * this ContentFormatter.
   *
   * This takes in a document to allow for Formatter chaining. However, in
   * chaining formatters, make sure that the output document of each formatter
   * can be parsed according to the specs of the next formatter.
   *
   * @param input
   *          The Element to format under this ContentFormatter's particular
   *          specification.
   * @return The formatted Document object.
   */
  Element format(Element input);

  /**
   * Converts a page into a particular parsed HTML Element based on the
   * specification in this ContentFormatter.
   *
   * This method calls Page.parsedContent() and passes the result directly to
   * the implemented format().
   *
   * @param page
   *          The page to format under this ContentFormatter's particular
   *          specification.
   * @return The formatted Document object.
   * @throws IOException
   *           If the parsedContent could not be accessed on the given page.
   */
  default Element format(T page) throws IOException {
    return format(page.parsedContent());
  }

  /**
   * Converts a page into a particular parsed HTML Element based on the
   * specification in this ContentFormatter, but uses the original version.
   *
   * This method calls Page.parsedContentOrignal() and passes the result
   * directly to the implemented format().
   *
   * Make sure not to manipulate the internal DOM of the page with
   * parsedContentOriginal()! This is included only to allow increased memory
   * efficiency by allowing an implemented to clone() only a subset of the
   * original DOM
   *
   * @param page
   *          The page to format under this ContentFormatter's particular
   *          specification.
   * @return The formatted Element object.
   * @throws IOException
   *           If the parsedContent could not be accessed on the given page.
   */
  default Element formatOriginal(T page) throws IOException {
    return format(page.parsedContentOriginal());
  }

  /**
   * Converts a page into a particular raw HTML string based on the
   * specification in this ContentFormatter.
   *
   * @param input
   *          The Element to format under this ContentFormatter's particular
   *          specification.
   * @return The raw HTML after formatting.
   */
  default String stringFormat(Element input) {
    return format(input).outerHtml();
  }

  /**
   * Converts a page into a particular raw HTML string based on the
   * specification in this ContentFormatter.
   *
   * @param page
   *          The page to format under this ContentFormatter's particular
   *          specification.
   * @return The raw HTML after formatting.
   * @throws IOException
   *           If the parsedContent could not be accessed on the given page.
   */
  default String stringFormat(T page) throws IOException {
    return format(page).outerHtml();
  }
}
