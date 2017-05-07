package edu.brown.cs.jmrs.server;

public class InputError extends Exception {

  private String message;

  public InputError(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
