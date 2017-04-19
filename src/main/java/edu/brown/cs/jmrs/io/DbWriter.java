package edu.brown.cs.jmrs.io;

import java.sql.PreparedStatement;
import java.util.function.BiConsumer;

/**
 * An abstraction representing how to identify uniquely and construct an object
 * from a database representation, i.e. a result set.
 *
 * Created by henry on 3/23/2017.
 *
 * @param <T>
 *          The type this DbReader reads in.
 */
public class DbWriter<T> {
  private BiConsumer<PreparedStatement, T> filler;

  /**
   * @param filler
   *          The function to populate a prepared statement with the fields
   *          corresponding to the object to be written using an {@link Insert}
   */
  public DbWriter(BiConsumer<PreparedStatement, T> filler) {
    this.filler = filler;
  }

  /**
   * @param ps
   *          The PreparedStatement to fill with obj for insertion into the
   *          database.
   * @param obj
   *          The obj to fill ps with.
   */
  public void fill(PreparedStatement ps, T obj) {
    filler.accept(ps, obj);
  }
}
