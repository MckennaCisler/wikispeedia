package edu.brown.cs.jmrs.server;

/**
 * An error allowing specification of the error message to be sent to the
 * client.
 *
 * @author shastin1
 *
 */
public class InputError extends Exception {

  private String message;

  /**
   * Constructor specifying error message for the client.
   *
   * @param message
   *          The message for the client
   */
  public InputError(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
