package edu.brown.cs.jmrs.collect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.jmrs.server.threading.ClosableReadWriteLock;

/**
 * A thread-safe bi-map. Allows effectively constant time lookups in both
 * directions on entries while also forcing both keys and values to be unique.
 *
 * @author shastin1
 *
 * @param <E>
 * @param <T>
 */
public class ConcurrentBiMap<E, T> implements Map<E, T> {

  private final ClosableReadWriteLock lock = new ClosableReadWriteLock();

  private Map<E, T>                   fore;
  private Map<T, E>                   back;

  /**
   * Default constructor, initializes forward and backward hash maps.
   */
  public ConcurrentBiMap() {
    fore = new HashMap<>();
    back = new HashMap<>();
  }

  @Override
  public void clear() {
    try (ClosableReadWriteLock temp = lock.lockWrite()) {
      fore.clear();
      back.clear();
    }
  }

  @Override
  public boolean containsKey(Object key) {
    boolean contains = false;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      contains = fore.containsKey(key);
    }
    return contains;
  }

  @Override
  public boolean containsValue(Object value) {
    boolean contains = false;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      contains = back.containsKey(value);
    }
    return contains;
  }

  @Override
  public Set<Entry<E, T>> entrySet() {
    Set<Entry<E, T>> entries = null;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      entries = fore.entrySet();
    }
    return entries;
  }

  @Override
  public T get(Object key) {
    T value = null;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      value = fore.get(key);
    }
    return value;
  }

  /**
   * Given a value, if there is an equivalent value held that value is returned.
   *
   * @param key
   *          The value to find an equivalent value to
   * @return The stored equivalent value, or null if none exists
   */
  public T getBack(T key) {
    T retVal = null;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      E tempVal = getReversed(key);
      if (tempVal != null) {
        retVal = get(tempVal);
      } else if (back.keySet().contains(key)) {
        for (T entry : back.keySet()) {
          if (key.equals(entry)) {
            retVal = entry;
            break;
          }
        }
      }
    }
    return retVal;
  }

  /**
   * Effectively constant time lookup of key from value.
   *
   * @param key
   *          The "value" to find the "key" for
   * @return The "key" associated with the given "value"
   */
  public E getReversed(Object key) {
    E value = null;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      value = back.get(key);
    }
    return value;
  }

  @Override
  public boolean isEmpty() {
    boolean empty = false;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      empty = fore.isEmpty();
    }
    return empty;
  }

  @Override
  public Set<E> keySet() {
    Set<E> keys = null;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      keys = fore.keySet();
    }
    return keys;
  }

  @Override
  public T put(E key, T value) {
    T old = null;
    try (ClosableReadWriteLock temp = lock.lockWrite()) {
      old = fore.put(key, value);
      back.remove(old);
      back.put(value, key);
    }
    return old;
  }

  @Override
  public void putAll(Map<? extends E, ? extends T> m) {
    try (ClosableReadWriteLock temp = lock.lockWrite()) {
      for (Entry<? extends E, ? extends T> entry : m.entrySet()) {
        E key = entry.getKey();
        T value = entry.getValue();
        T old = fore.put(key, value);
        back.remove(old);
        back.put(value, key);
      }
    }
  }

  /**
   * Puts key-value pair into map, but only if the given value is not already
   * associated with any key.
   *
   * @param key
   *          The key to enter
   * @param value
   *          The value to enter
   * @return True iff the key-value pair was successfully put into the map
   */
  public boolean putNoOverwrite(E key, T value) {
    boolean placed = false;
    try (ClosableReadWriteLock temp = lock.lockWrite()) {
      if (getReversed(value) == null) {
        placed = true;
        put(key, value);
      }
    }
    return placed;
  }

  @Override
  public T remove(Object key) {
    T old = null;
    try (ClosableReadWriteLock temp = lock.lockWrite()) {
      old = fore.remove(key);
      back.remove(old);
    }
    return old;
  }

  @Override
  public int size() {
    int size = 0;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      size = fore.size();
    }
    return size;
  }

  @Override
  public String toString() {
    String retVal = null;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      retVal = fore.toString();
    }
    return retVal;
  }

  @Override
  public Collection<T> values() {
    Set<T> keys = null;
    try (ClosableReadWriteLock temp = lock.lockRead()) {
      keys = back.keySet();
    }
    return keys;
  }
}
