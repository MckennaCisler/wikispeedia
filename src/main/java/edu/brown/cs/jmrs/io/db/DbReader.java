package edu.brown.cs.jmrs.io.db;

import java.sql.ResultSet;

/**
 * An abstraction representing how to identify uniquely and construct an object
 * from a database representation, i.e. a result set.
 *
 * Created by henry on 3/23/2017.
 *
 * @param <T>
 *          The type this DbReader reads in.
 */
public class DbReader<T> {
  private ThrowingFunction<ResultSet, T> constructor;

  /**
   * @param constructor
   *          The function to be used to create an object for this DbReader from
   *          a result set.
   */
  public DbReader(ThrowingFunction<ResultSet, T> constructor) {
    this.constructor = constructor;
  }

  /**
   * @param rs
   *          The result set to deserialize an object from.
   * @return The constructed object.
   */
  public T construct(ResultSet rs) {
    return constructor.apply(rs);
  }
}
