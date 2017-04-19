package edu.brown.cs.jmrs.io.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides a simpler interface to normal SQL database calls, as well as
 * built-in level 1 caching based on queries.
 *
 * @author mcisler
 */
public class DbConn implements AutoCloseable {
  private final ThreadLocal<Connection> conn;
  // set of all thread connections for single thread closing
  private final Set<Connection> conns;
  /*
   * Maps from query to a cache of the output returned by a query callback on a
   * particular input.
   */

  /**
   * Constructs a DbConn out of the provided Sqlite3 database, to be ready for
   * querying.
   *
   * @param path
   *          The path to the .sqlite3 database to load.
   * @throws ClassNotFoundException
   *           If the driver for Sqlite3 could not be loaded.
   * @throws UnsupportedOperationException
   *           If the database was not a .sqlite3 database.
   * @throws FileNotFoundException
   *           If database could not be found.
   */
  public DbConn(String path)
      throws ClassNotFoundException, FileNotFoundException {
    Class.forName("org.sqlite.JDBC");
    conns = new HashSet<>();

    // try to open the database (don't use ones that aren't there yet)
    if (path.lastIndexOf('.') == -1
        || !path.substring(path.lastIndexOf('.')).equals(".sqlite3")) {
      throw new UnsupportedOperationException(
          "Database must be a sqlite3 database.");
    } else if (!new File(path).exists()) {
      throw new FileNotFoundException("Database not found at " + path);
    }

    String urlToDB = "jdbc:sqlite:" + path;
    conn = ThreadLocal.withInitial(() -> {
      try {
        Connection c = DriverManager.getConnection(urlToDB);
        try (Statement stat = c.createStatement()) {
          stat.executeUpdate("PRAGMA foreign_keys = ON;");
        }
        conns.add(c);
        return c;
      } catch (SQLException e) {
        throw new UncheckedSqlException(e);
      }
    });
  }

  /**
   * Makes a query out of string and an DbReader to read objects.
   *
   * @param query
   *          The query to create from.
   * @param reader
   *          The reader to construct and cache objects from the query.
   * @return A query object that can be executed.
   * @param <T>
   *          The type of object to the query will return.
   */
  public <T> Query<T> makeQuery(final String query, DbReader<T> reader) {
    ThreadLocal<PreparedStatement> p = ThreadLocal.withInitial(() -> {
      try {
        return conn.get().prepareStatement(query);
      } catch (SQLException e) {
        throw new UncheckedSqlException(e);
      }
    });
    return new Query<T>(p, reader);
  }

  /**
   * Makes a query out of string and an DbReader to read objects, with a
   * specification for whether to cache results from the query.
   *
   * @param query
   *          The query to create from.
   * @param reader
   *          The reader to construct and optionally cache objects from the
   *          query.
   * @param save
   *          Whether to cache returned objects.
   * @return The new Query.
   * @param <T>
   *          The type of object to the query will return.
   */
  public <T> Query<T> makeQuery(final String query, DbReader<T> reader,
      boolean save) {
    ThreadLocal<PreparedStatement> p = ThreadLocal.withInitial(() -> {
      try {
        return conn.get().prepareStatement(query);
      } catch (SQLException e) {
        throw new UncheckedSqlException(e);
      }
    });
    return new Query<T>(p, reader, save);
  }

  /**
   *
   * @param insertStatement
   *          The query to insert using.
   * @param writer
   *          The DbWriter to use to write objects to the prepared statement.
   * @return The new Insert.
   * @param <T>
   *          The type of object to the query will insert.
   */
  public <T> Insert<T> makeInsert(final String insertStatement,
      DbWriter<T> writer) {
    ThreadLocal<PreparedStatement> p = ThreadLocal.withInitial(() -> {
      try {
        return conn.get().prepareStatement(insertStatement);
      } catch (SQLException e) {
        throw new UncheckedSqlException(e);
      }
    });
    return new Insert<T>(p, writer);
  }

  @Override
  public void close() throws Exception {
    for (Connection c : conns) {
      c.close();
    }
  }
}
