package edu.brown.cs.jmrs.server.errorhandling;

public class StaticErrorHandler {

  public static void _assert(boolean assertion, String failureMessage)
      throws ServerError {
    if (!assertion) {
      throw new ServerError(failureMessage);
    }
  }
}
