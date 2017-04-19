package edu.brown.cs.jmrs.io;

import java.sql.SQLException;

/**
 * Created by henry on 3/23/2017.
 */
public class UncheckedSqlException extends RuntimeException {
  /**
   * To implement Serializable.
   */
  private static final long serialVersionUID = -9173711673522899065L;

  /**
   * @param e
   *          The wrapped SQLException.
   */
  public UncheckedSqlException(SQLException e) {
    super(e);
  }
}
