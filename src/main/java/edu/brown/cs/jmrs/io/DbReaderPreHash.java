package edu.brown.cs.jmrs.io;

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
  private static final int MAX_SAVED_PREHASH = 1000000;

  private ThrowingFunction<ResultSet, H> prehash;
  private ConcurrentMap<H, T> madeObjects;

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
    super(callback);
    this.prehash = prehash;
    this.madeObjects = new ConcurrentHashMap<>();
  }

  @Override
  public T construct(ResultSet rs) {
    H ph = prehash.apply(rs);
    if (madeObjects.containsKey(ph)) {
      return madeObjects.get(ph);
    } else {
      T t = super.construct(rs);
      // avoid out of memory error
      if (madeObjects.size() < MAX_SAVED_PREHASH) {
        madeObjects.putIfAbsent(ph, t);
      } else {
        madeObjects.clear();
      }
      return t;
    }
  }
}
