package edu.brown.cs.jmrs.io.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * An interface defining the funcitonality requried for an object to be read
 * from and cached in a SQL database.
 *
 * @author mcisler
 *
 * @param <T>
 *          The type of Object in this DbObject.
 * @param <H>
 *          The type of the hash (unique id) for this object.
 */
@Deprecated
public interface DbObject<T, H> {
  /**
   * Constructs (deserializes) the object out of ResultSet, returned from a
   * Query.
   *
   * @param rs
   *          The result set to deserialize an object from.
   * @return The constructed object.
   */
  T construct(ResultSet rs);

  /**
   * A function to use to associate a resultset to an object without
   * constructing it (for cache lookups).
   *
   * @param rs
   *          The result set that may be this object.
   * @return A hashCode uniquely representing the object that would be (or is
   *         already) constructed from the given result set.
   */
  H prehash(ResultSet rs);

  /**
   * Populates a prepared statement with the fields corresponding to this object
   * when written to the database.
   *
   * @param ps
   *          The prepared statement to populate only with the fields of this
   *          object.
   */
  void fill(PreparedStatement ps);
}
