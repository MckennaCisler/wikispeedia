package edu.brown.cs.jmrs.io;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A class wrapping a SQL Insert OR Insert, including adding created objects to
 * cache. Note that this is NOT only functional for a SQL UPDATE command.
 *
 * @author mcisler
 *
 * @param <T>
 *          The type this Query returns.
 */
public class Insert<T> extends DbStatement<T> {
  private final DbWriter<T> objectWriter;

  /**
   * Constructs an Insert to write objects to the database.
   *
   * @param p
   *          The PrepareStatement representing the insert/update.
   * @param objectWriter
   *          The DbWriter to write objecst to the PreparedStatement
   */
  public Insert(ThreadLocal<PreparedStatement> p, DbWriter<T> objectWriter) {
    super(p);
    this.objectWriter = objectWriter;
  }

  /**
   * Inserts using this the prepared statement filled with inputs.
   *
   * @param object
   *          The object to insert into the database.
   */
  public void insert(T object) {
    PreparedStatement ps = getLocalPreparedStatement();
    objectWriter.fill(ps, object);
    try {
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new UncheckedSqlException(e);
    }
  }
}
