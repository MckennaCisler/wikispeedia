package edu.brown.cs.jmrs.io.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
  private final ThrowingBiConsumer<PreparedStatement, T> filler;
  private final String setupStatement;

  /**
   * @param filler
   *          The function to populate a prepared statement with the fields
   *          corresponding to the object to be written using an {@link Insert}
   */

  public DbWriter(ThrowingBiConsumer<PreparedStatement, T> filler) {
    this(filler, null);
  }

  /**
   * @param filler
   *          The function to populate a prepared statement with the fields
   *          corresponding to the object to be written using an {@link Insert}
   * @param setupStatement
   *          A query to run before running any insertions using this DbWriter.
   *          Use to setup a table if it may not exist.
   */

  public DbWriter(ThrowingBiConsumer<PreparedStatement, T> filler,
      String setupStatement) {
    this.filler = filler;
    this.setupStatement = setupStatement;

  }

  /**
   * Executes the setup prepared statement defined on this object, if it was
   * included.
   *
   * @param conn
   *          The database Connection to execute the setupStatement on.
   * @throws SQLException
   *           If there is an error in executing the statement.
   */
  public void setup(Connection conn) throws SQLException {
    if (setupStatement != null) {
      conn.prepareStatement(setupStatement).executeUpdate();
    }
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
