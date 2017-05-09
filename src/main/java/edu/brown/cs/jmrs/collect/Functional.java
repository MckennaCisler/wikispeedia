package edu.brown.cs.jmrs.collect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Function;

/**
 * A collection of methods supplementing normal java functionality.
 *
 * @author mcisler
 *
 */
public final class Functional {

  private Functional() {
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

  /**
   * @param input
   *          The input set of values.
   * @param p
   *          The predicate to remove values based on.
   * @return A set with all elements on which predicate evaluated to false
   *         removed.
   * @param <T>
   *          The input type.
   */
  public static <T> Set<T> filter(Set<T> input, Predicate<T> p) {
    Set<T> output = new HashSet<>(input.size());
    input.forEach(i -> {
      if (p.test(i)) {
        output.add(i);
      }
    });
    return output;
  }

  /**
   * @param input
   *          The input set of values.
   * @param p
   *          The predicate to remove values based on.
   * @return A list with all elements on which predicate evaluated to false
   *         removed.
   * @param <T>
   *          The input type.
   */
  public static <T> List<T> filter(List<T> input, Predicate<T> p) {
    List<T> output = new ArrayList<>(input.size());
    input.forEach(i -> {
      if (p.test(i)) {
        output.add(i);
      }
    });
    return output;
  }

  /**
   * @param input
   *          The input set of values.
   * @param p
   *          The predicate to remove values based on.
   * @return A Collection with all elements on which predicate evaluated to
   *         false removed.
   * @param <T>
   *          The input type.
   */
  public static <T> Collection<T> filter(Collection<T> input, Predicate<T> p) {
    Collection<T> output = new ArrayList<>(input.size());
    input.forEach(i -> {
      if (p.test(i)) {
        output.add(i);
      }
    });
    return output;
  }

}
