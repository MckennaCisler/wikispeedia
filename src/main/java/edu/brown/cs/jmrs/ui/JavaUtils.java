package edu.brown.cs.jmrs.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;

/**
 * A collection of methods supplementing normal java functionality.
 *
 * @author mcisler
 *
 */
public final class JavaUtils {

  private JavaUtils() {
    // override default constructor
  }

  /**
   * @param input
   *          The input set of values.
   * @param f
   *          The function to map values of input to output values.
   * @return A list with the output of f corresponding to each input element in
   *         input.
   * @param <T>
   *          The input type.
   * @param <V>
   *          The output type.
   */
  public static <T, V> Set<V> map(Set<T> input, Function<T, V> f) {
    Set<V> output = new HashSet<>(input.size());
    input.forEach(i -> output.add(f.apply(i)));
    return output;
  }

  /**
   * @param input
   *          The input list of values.
   * @param f
   *          The function to map values of input to output values.
   * @return A list with the output of f corresponding to each input element in
   *         input.
   * @param <T>
   *          The input type.
   * @param <V>
   *          The output type.
   */
  public static <T, V> List<V> map(List<T> input, Function<T, V> f) {
    List<V> output = new ArrayList<>(input.size());
    input.forEach(i -> output.add(f.apply(i)));
    return output;
  }

  /**
   * @param input
   *          The input collection of values.
   * @param f
   *          The function to map values of input to output values.
   * @return A list with the output of f corresponding to each input element in
   *         input.
   * @param <T>
   *          The input type.
   * @param <V>
   *          The output type.
   */
  public static <T, V> Collection<V> map(Collection<T> input,
      Function<T, V> f) {
    return map(new ArrayList<>(input), f);
  }
}
