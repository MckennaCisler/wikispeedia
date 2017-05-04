package edu.brown.cs.jmrs.io.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

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
   * Inserts using this insert's prepared statement filled with inputs.
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

  /**
   * Inserts using this insert's prepared statement filled with inputs.
   *
   * @param objs
   *          The series of object to insert into the database in a batch
   *          command.
   */
  public void insertAll(Collection<T> objs) {
    PreparedStatement ps = getLocalPreparedStatement();
    try {
      for (T object : objs) {
        objectWriter.fill(ps, object);
        ps.addBatch();
      }

      ps.executeBatch();
    } catch (SQLException e) {
      throw new UncheckedSqlException(e);

    }
  }
}
