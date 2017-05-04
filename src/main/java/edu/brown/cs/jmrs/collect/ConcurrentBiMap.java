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
    int size = 0;
    try {
      r.lock();
      size = fore.size();
    } finally {
      r.unlock();
    }
    return size;
  }

  @Override
  public boolean isEmpty() {
    boolean empty = false;
    try {
      r.lock();
      empty = fore.isEmpty();
    } finally {
      r.unlock();
    }
    return empty;
  }

  @Override
  public boolean containsKey(Object key) {
    boolean contains = false;
    try {
      r.lock();
      contains = fore.containsKey(key);
    } finally {
      r.unlock();
    }
    return contains;
  }

  @Override
  public boolean containsValue(Object value) {
    boolean contains = false;
    try {
      r.lock();
      contains = back.containsKey(value);
    } finally {
      r.unlock();
    }
    return contains;
  }

  @Override
  public T get(Object key) {
    T value = null;
    try {
      r.lock();
      value = fore.get(key);
    } finally {
      r.unlock();
    }
    return value;
  }

  public E getReversed(Object key) {
    E value = null;
    try {
      r.lock();
      value = back.get(key);
    } finally {
      r.unlock();
    }
    return value;
  }

  public T getBack(T key) {
    T retVal = null;
    try {
      r.lock();
      retVal = get(getReversed(key));
    } finally {
      r.unlock();
    }
    return retVal;
  }

  @Override
  public T put(E key, T value) {
    T old = null;
    try {
      w.lock();
      old = fore.put(key, value);
      back.remove(old);
      back.put(value, key);
    } finally {
      w.unlock();
    }
    return old;
  }

  public boolean putNoOverwrite(E key, T value) {
    boolean placed = false;
    try {
      w.lock();
      if (!back.containsKey(value)) {
        placed = true;
        put(key, value);
      }
    } finally {
      w.unlock();
    }
    return placed;
  }

  @Override
  public T remove(Object key) {
    T old = null;
    try {
      w.lock();
      old = fore.remove(key);
      back.remove(old);
    } finally {
      w.unlock();
    }
    return old;
  }

  @Override
  public void putAll(Map<? extends E, ? extends T> m) {
    try {
      w.lock();
      for (Entry<? extends E, ? extends T> entry : m.entrySet()) {
        E key = entry.getKey();
        T value = entry.getValue();
        T old = fore.put(key, value);
        back.remove(old);
        back.put(value, key);
      }
    } finally {
      w.unlock();
    }
  }

  @Override
  public void clear() {
    try {
      w.lock();
      fore.clear();
      back.clear();
    } finally {
      w.unlock();
    }
  }

  @Override
  public Set<E> keySet() {
    Set<E> keys = null;
    try {
      r.lock();
      keys = fore.keySet();
    } finally {
      r.unlock();
    }
    return keys;
  }

  @Override
  public Collection<T> values() {
    Set<T> keys = null;
    try {
      r.lock();
      keys = back.keySet();
    } finally {
      r.unlock();
    }
    return keys;
  }

  @Override
  public Set<Entry<E, T>> entrySet() {
    Set<Entry<E, T>> entries = null;
    try {
      r.lock();
      entries = fore.entrySet();
    } finally {
      r.unlock();
    }
    return entries;
  }

  @Override
  public String toString() {
    String retVal = null;
    try {
      r.lock();
      retVal = fore.toString();
    } finally {
      r.unlock();
    }
    return retVal;
  }
}
