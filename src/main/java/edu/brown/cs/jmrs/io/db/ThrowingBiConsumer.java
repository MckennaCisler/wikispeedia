package edu.brown.cs.jmrs.io.db;

import java.util.function.BiConsumer;

/**
 * A BiConsumer implementation that can wrap another BiConsumer to allow
 * unchecked exceptions. Used in DbWriter.
 *
 * Note: based partially off http://stackoverflow.com/a/27252163
 *
 * @author mcisler
 *
 * @param <A>
 *          The first input type of the function.
 * @param <B>
 *          The second input type of the function.
 */
@FunctionalInterface
public interface ThrowingBiConsumer<A, B> extends BiConsumer<A, B> {
  @Override
  default void accept(A a, B b) {
    try {
      applyThrows(a, b);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param a
   *          The first input to the function.
   * @param b
   *          The second input to the function.
   * @throws Exception
   *           If the interior apply throws an exception.
   */
  void applyThrows(A a, B b) throws Exception;
}
