package edu.brown.cs.jmrs.collect;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Function;

/**
 * A class that memoizes input so that lookups only need to be run once, and
 * further calls are returned from memory (the cache). Uses a lookupMethod to
 * get the data from the actual source.
 *
 * Thread safe through use of a ConcurrentHashMap.
 *
 * @author mcisler
 *
 * @param <K>
 *          The key type; what is passed to the provided lookupMethod to
 *          generate a value.
 * @param <V>
 *          The value type; what is returned by the provided lookupMethod.
 *
 */
public class Cache<K, V> {
  private final ConcurrentMap<K, V> cachedValues;
  private final Function<K, V> lookupMethod;
  private final int maxSize;

  /**
   * Constructs a lookup method to cache outputs from the provided function for
   * potentially quicker access on subsequent calls.
   *
   * @param lookupMethod
   *          The function to be used to map keys to values. Essentally called
   *          when get() is called.
   */
  public Cache(Function<K, V> lookupMethod) {
    this.cachedValues = new ConcurrentHashMap<>();
    this.lookupMethod = lookupMethod;
    this.maxSize = Integer.MAX_VALUE;
  }

  /**
   * Constructs a lookup method to cache outputs from the provided function for
   * potentially quicker access on subsequent calls. Also clears the cache after
   * a particular number of entries are added.
   *
   * @param lookupMethod
   *          The function to be used to map keys to values. Essentally called
   *          when get() is called.
   * @param maxSize
   *          The maximum number of elements to hold before emptying the cache.
   */
  public Cache(Function<K, V> lookupMethod, int maxSize) {
    this.cachedValues = new ConcurrentHashMap<>();
    this.lookupMethod = lookupMethod;
    this.maxSize = maxSize;
  }

  /**
   * Gets the value corresponding to the given value according to the provided
   * lookupMethod. Looks up the value in the cache if it already exists, and
   * only defers to lookupMethod when necessary.
   *
   * @param key
   *          Argument to the lookupMethod.
   * @return The value lookupMethod returns when given key.
   */
  public V get(K key) {
    if (cachedValues.containsKey(key)) {
      return cachedValues.get(key);
    }
    V val = lookupMethod.apply(key);
    put(key, val);
    return val;
  }

  /**
   * Manually puts a value into this cache, overwriting any previous value. This
   * value may be cleared if the cache exceeds maximum size.
   *
   *
   * @param key
   *          Argument to the lookupMethod, the key to correspond with value.
   * @param value
   *          The object to put into the cache.
   * @return The value previously stored under key, or null if none was.
   */
  public V put(K key, V value) {
    if (cachedValues.size() > maxSize) {
      cachedValues.clear();
    }
    return cachedValues.putIfAbsent(key, value);
  }
}
