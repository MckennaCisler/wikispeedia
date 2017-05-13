package edu.brown.cs.jmrs.io.db;

import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A database reader that will keep track of the objects it has loaded by some
 * aspect of the object (typically the id), and avoid reloading the same object
 * into memory. Created by henry on 4/10/2017.
 *
 * @param <T>
 *          the object that this creates.
 * @param <H>
 *          the hash type that prehash creates.
 */
public class DbReaderPreHash<T, H> extends DbReader<T> {
  // Avoid running out of memory due to cache size
  private static final int DEFAULT_CACHE_SIZE = 100000;

  private ThrowingFunction<ResultSet, H> prehash;
  private ConcurrentMap<H, T> madeObjects;
  private final int cacheSize;

  /**
   * @param callback
   *          The function to be used to create an object for this DbReader from
   *          a result set.
   * @param prehash
   *          A function that returns a hashcode uniquely representing the
   *          object that would be (or is already) constructed from the given
   *          result set.
   */
  public DbReaderPreHash(ThrowingFunction<ResultSet, T> callback,
      ThrowingFunction<ResultSet, H> prehash) {
    this(callback, prehash, DEFAULT_CACHE_SIZE);
  }

  /**
   * @param callback
   *          The function to be used to create an object for this DbReader from
   *          a result set.
   * @param prehash
   *          A function that returns a hashcode uniquely representing the
   *          object that would be (or is already) constructed from the given
   *          result set.
   * @param cacheSize
   *          The number of objects to store in the cache.
   */
  public DbReaderPreHash(ThrowingFunction<ResultSet, T> callback,
      ThrowingFunction<ResultSet, H> prehash, int cacheSize) {
    super(callback);
    this.prehash = prehash;
    this.madeObjects = new ConcurrentHashMap<>();
    this.cacheSize = cacheSize;
  }

  @Override
  public T construct(ResultSet rs) {
    H ph = prehash.apply(rs);
    if (madeObjects.containsKey(ph)) {
      return madeObjects.get(ph);
    } else {
      T t = super.construct(rs);
      // avoid out of memory error
      if (madeObjects.size() < cacheSize) {
        madeObjects.put(ph, t);
      } else {
        madeObjects.clear();
      }
      return t;
    }
  }
}
