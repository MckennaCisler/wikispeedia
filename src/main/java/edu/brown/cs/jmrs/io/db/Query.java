package edu.brown.cs.jmrs.io.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.brown.cs.jmrs.collect.Cache;

/**
 * A class wrapping a SQL Query, including caching returned results as
 * appropriate objects.
 *
 * Created by henry on 3/23/2017.
 *
 * @param <T>
 *          The type this Query returns.
 */
public class Query<T> extends DbStatement<T> {
  // Avoid running out of memory due to cache size
  private static final int MAX_SAVED_QUERIES = 100000;

  private DbReader<T> objectReader;
  private Cache<ObjArray, List<T>> fullyQueryResponse;
  private boolean save;

  /**
   * Constructs a query to read objects using p and cache them.
   *
   * @param p
   *          The PrepareStatement representing the query.
   * @param objectReader
   *          The DbReader to be used to construct and prehash objects returned
   *          from the query.
   */
  public Query(ThreadLocal<PreparedStatement> p, DbReader<T> objectReader) {
    this(p, objectReader, true);
  }

  /**
   * Constructs a query to read objects using p and optionally cache them.
   *
   * @param p
   *          The PrepareStatement representing the query.
   * @param objectReader
   *          The DbReader to be used to construct and prehash objects returned
   *          from the query.
   * @param save
   *          Whether to cache the objects returned by the query.
   */
  public Query(ThreadLocal<PreparedStatement> p, DbReader<T> objectReader,
      boolean save) {
    super(p);
    this.objectReader = objectReader;
    if (save) {
      this.fullyQueryResponse =
          new Cache<>((objArr) -> queryDb(objArr), MAX_SAVED_QUERIES);
    }
    this.save = save;
  }

  /**
   * Queries using this Query's prepared statement filled with inputs.
   *
   * @param inputs
   *          The inputs to substitute in order into the parameters of the
   *          prepared statement.
   * @return The list of returned objects from the query.
   */
  public synchronized List<T> query(Object... inputs) {
    ObjArray hashableInputs = new ObjArray(inputs);
    if (save) {
      return fullyQueryResponse.get(hashableInputs);
    } else {
      return queryDb(hashableInputs);
    }
  }

  private List<T> queryDb(ObjArray inputs) {
    ResultSet rs = null;
    PreparedStatement p = super.getLocalPreparedStatement();
    try {
      for (int i = 0; i < inputs.get().length; i++) {
        p.setObject(i + 1, inputs.get()[i]);
      }

      List<T> response = new LinkedList<>();
      rs = p.executeQuery();

      while (rs.next()) {
        T d = objectReader.construct(rs);
        if (d != null) {
          response.add(d);
        }
      }

      rs.close();
      return response;

    } catch (SQLException e) {
      throw new UncheckedSqlException(e);
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          System.out.println("ERROR: closing result set failed");
        }
      }
    }
  }
}
