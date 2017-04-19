package edu.brown.cs.jmrs.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jsoup.nodes.Element;

/**
 * A helper class for ContentFormatter which encapsulates the chaining of
 * multiple formatters to create an aggregate Formatter.
 *
 * @author mcisler
 *
 * @param <T>
 *          The subtype of page this ContentFormatterChain formats.
 */
public class ContentFormatterChain<T extends Page>
    extends ArrayList<ContentFormatter<T>> implements ContentFormatter<T> {

  /**
   * Constructs a ContentFormatterChain which applies formatters in order.
   *
   * @param formatters
   *          A Collection of ContentFormatters, ordered from first call to last
   *          (on the input object to .format())
   */
  public ContentFormatterChain(Collection<ContentFormatter<T>> formatters) {
    super(formatters);
  }

  /**
   * To implement Serializable.
   */
  private static final long serialVersionUID = -8536757113004552982L;

  @Override
  public Element format(Element input) {
    Element curElement = input;
    Iterator<ContentFormatter<T>> iterator = super.iterator();
    while (iterator.hasNext()) {
      curElement = iterator.next().format(input);
    }
    return curElement;
  }
}
