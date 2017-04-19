package edu.brown.cs.jmrs.io;

import com.google.common.base.Function;

/**
 * A Function implementation that can wrap another Function to allow unchecked
 * exceptions.
 *
 * Note: based partially off http://stackoverflow.com/a/27252163
 *
 * @author mcisler
 *
 * @param <F>
 *          The input type of the function
 * @param <T>
 *          The return type of the function.
 */
@FunctionalInterface
public interface ThrowingFunction<F, T> extends Function<F, T> {
  @Override
  default T apply(F input) {
    try {
      return applyThrows(input);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param input
   *          The input to the function
   * @return The output of the function.
   * @throws Exception
   *           If the interior apply throws an exception.
   */
  T applyThrows(F input) throws Exception;
}
