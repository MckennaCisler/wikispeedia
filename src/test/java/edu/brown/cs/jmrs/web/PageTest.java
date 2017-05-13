package edu.brown.cs.jmrs.web;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PageTest {

  @Test
  public void testCleanUrl() {
    assertEquals("https://en.wikipedia.org",
        Page.cleanUrl("https://en.wikipedia.org"));
    assertEquals("https://en.wikipedia.org/cats",
        Page.cleanUrl("https://en.wikipedia.org/cats"));
    assertEquals("https://en.wikipedia.org/wiki/Cats/",
        Page.cleanUrl("https://en.wikipedia.org/wiki/Cats/"));
    assertEquals("https://en.wikipedia.org/wiki/Cats",
        Page.cleanUrl("https://en.wikipedia.org/wiki/Cats#animals"));
    assertEquals("https://en.wikipedia.org/wiki/Cats Are Cool/",
        Page.cleanUrl("https://en.wikipedia.org/wiki/Cats%20Are%20Cool/"));
    assertEquals("https://en.wikipedia.org/wiki/Cats",
        Page.cleanUrl("https://en.wikipedia.org/wiki/Cats?cool=true"));
    assertEquals("https://en.wikipedia.org/wiki/Cats_are_always_welcome",
        Page.cleanUrl("https://en.wikipedia.org/wiki/Cats_are_always_welcome"));
    assertEquals("https://en.wikipedia.org/wiki/Dog's_cats_are_welcome",
        Page.cleanUrl("https://en.wikipedia.org/wiki/Dog's_cats_are_welcome"));
    assertEquals("https://en.wikipedia.org/wiki/dogs_don't_die",
        Page.cleanUrl("https://en.wikipedia.org/wiki/dogs_\"don't\"_die"));

  }

  @Test
  public void testUrlEndString() {
    assertEquals("", Page.urlEnd(""));
    assertEquals("", Page.urlEnd("/"));
    assertEquals("", Page.urlEnd("https://en.wikipedia.org"));
    assertEquals("", Page.urlEnd("https://en.wikipedia.org/"));
    assertEquals("cats", Page.urlEnd("https://en.wikipedia.org/cats"));
    assertEquals("cats", Page.urlEnd("https://en.wikipedia.org/cats/"));
    assertEquals("cats_galore",
        Page.urlEnd("https://en.wikipedia.org/wiki/cats_galore"));
    assertEquals("cats_galore those cats",
        Page.urlEnd("https://en.wikipedia.org/wiki/cats_galore those cats/"));
    assertEquals("", Page.urlEnd("https://"));
  }
}
