package edu.brown.cs.jmrs.web.wikipedia;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WikiPageLinkFinderTest {

  @Test
  public void testIsDate() {
    assertTrue(WikiPageLinkFinder.isDate("http://en.wikipedia.org/July_1"));
    assertTrue(
        WikiPageLinkFinder.isDate("http://en.wikipedia.org/September_31"));
    assertTrue(
        WikiPageLinkFinder.isDate("https://en.wikipedia.org/September_31"));
    assertTrue(WikiPageLinkFinder.isDate("https://fr.wikipedia.org/October_8"));
    assertTrue(
        WikiPageLinkFinder.isDate("https://simple.wikipedia.org/September_31"));
    assertTrue(
        WikiPageLinkFinder.isDate("https://simple.wikipedia.org/SEPTEMBER_2"));
    assertTrue(
        WikiPageLinkFinder.isDate("https://simple.wikipedia.org/november_31"));
    assertTrue(
        WikiPageLinkFinder.isDate("https://simple.wikipedia.org/april_05"));
    assertFalse(
        WikiPageLinkFinder.isDate("https://simple.wikipedia.org/July_the_5th"));
    assertFalse(WikiPageLinkFinder.isDate("https://simple.wikipedia.org/Cats"));
    assertFalse(WikiPageLinkFinder
        .isDate("https://simple.wikipedia.org/March_Madness"));
    assertFalse(
        WikiPageLinkFinder.isDate("https://simple.wikipedia.org/Marchy"));
    assertFalse(WikiPageLinkFinder.isDate("November_forever"));
    assertFalse(WikiPageLinkFinder.isDate("The_December_crew"));
    assertFalse(WikiPageLinkFinder.isDate("The_July_29_crew"));
  }
}
