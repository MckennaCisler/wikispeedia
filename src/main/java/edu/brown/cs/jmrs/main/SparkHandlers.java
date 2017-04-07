package edu.brown.cs.jmrs.main;

import spark.template.freemarker.FreeMarkerEngine;

/**
 * A simple interface to encapsulate the creation/registering of SparkHandlers.
 *
 * @author mcisler
 *
 */
public interface SparkHandlers {
  /**
   * Registers the handlers for this SparkHandlers object..
   *
   * @param freeMarker
   *          The template engine to use when registering handlers.
   */
  void registerHandlers(FreeMarkerEngine freeMarker);
}
