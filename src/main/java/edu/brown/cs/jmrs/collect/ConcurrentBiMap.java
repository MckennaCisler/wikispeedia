package edu.brown.cs.jmrs.collect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentBiMap<E, T> implements Map<E, T> {

  private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
  private final Lock                   r   = rwl.readLock();
  private final Lock                   w   = rwl.writeLock();

  private Map<E, T>                    fore;
  private Map<T, E>                    back;

  public ConcurrentBiMap() {
    fore = new HashMap<>();
    back = new HashMap<>();
  }

  @Override
  public int size() {
    r.lock();
    int size = fore.size();
    r.unlock();
    return size;
  }

  @Override
  public boolean isEmpty() {
    r.lock();
    boolean empty = fore.isEmpty();
    r.unlock();
    return empty;
  }

  @Override
  public boolean containsKey(Object key) {
    r.lock();
    boolean contains = fore.containsKey(key);
    r.unlock();
    return contains;
  }

  @Override
  public boolean containsValue(Object value) {
    r.lock();
    boolean contains = back.containsKey(value);
    r.unlock();
    return contains;
  }

  @Override
  public T get(Object key) {
    r.lock();
    T value = fore.get(key);
    r.unlock();
    return value;
  }

  public E getReversed(Object key) {
    r.lock();
    E value = back.get(key);
    r.unlock();
    return value;
  }

  public T getBack(Object key) {
    r.lock();
    if (containsValue(key)) {
      Set<T> vals = back.keySet();

      for (T val : vals) {
        if (val.equals(key)) {
          return val;
        }
      }
    }
    r.unlock();
    return null;
  }

  @Override
  public T put(E key, T value) {
    w.lock();
    T old = fore.put(key, value);
    back.remove(old);
    back.put(value, key);
    w.unlock();
    return old;
  }

  public boolean putNoOverwrite(E key, T value) {
    boolean placed = false;
    w.lock();
    if (!back.containsKey(value)) {
      placed = true;
      put(key, value);
    }
    w.unlock();
    return placed;
  }

  @Override
  public T remove(Object key) {
    w.lock();
    T old = fore.remove(key);
    back.remove(old);
    w.unlock();
    return old;
  }

  @Override
  public void putAll(Map<? extends E, ? extends T> m) {
    w.lock();
    for (Entry<? extends E, ? extends T> entry : m.entrySet()) {
      E key = entry.getKey();
      T value = entry.getValue();
      T old = fore.put(key, value);
      back.remove(old);
      back.put(value, key);
    }
    w.unlock();
  }

  @Override
  public void clear() {
    w.lock();
    fore.clear();
    back.clear();
    w.unlock();
  }

  @Override
  public Set<E> keySet() {
    r.lock();
    Set<E> keys = fore.keySet();
    r.unlock();
    return keys;
  }

  @Override
  public Collection<T> values() {
    r.lock();
    Set<T> keys = back.keySet();
    r.unlock();
    return keys;
  }

  @Override
  public Set<Entry<E, T>> entrySet() {
    r.lock();
    Set<Entry<E, T>> entries = fore.entrySet();
    r.unlock();
    return entries;
  }
}
