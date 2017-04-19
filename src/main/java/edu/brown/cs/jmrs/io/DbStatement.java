package edu.brown.cs.jmrs.io;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A general class for DB queries, insertions, and updates. Classes such as
 * {@link Query} and {@link Insert} extend this class with more specific
 * functionality, while this class primarily stores the generic prepared
 * allStatements.
 *
 * @author mcisler
 * @param <T>
 *          THe type of object this query operates on.
 */
public abstract class DbStatement<T> implements AutoCloseable {
  // List of all prepared allStatements that will need to be closed
  private Set<PreparedStatement> allStatements;
  private ThreadLocal<PreparedStatement> ps;

  /**
   * Constructs a DbStatement ready to read or create objects using p.
   *
   * @param p
   *          The PrepareStatement representing the query.
   */
  public DbStatement(ThreadLocal<PreparedStatement> p) {
    this.ps = p;
    allStatements = new HashSet<>();
  }

  PreparedStatement getLocalPreparedStatement() {
    PreparedStatement p = ps.get();
    allStatements.add(p);
    return p;
  }

  @Override
  public void close() throws Exception {
    for (PreparedStatement p : allStatements) {
      p.close();
    }
  }

  /**
   * Array of objects.
   *
   * @author mcisler
   *
   */
  static final class ObjArray {
    private Object[] objects;

    /**
     * default constructor.
     *
     * @param objs
     *          objects to make ObjArray out of
     */
    ObjArray(Object[] objs) {
      this.objects = objs;
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(objects);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof ObjArray) {
        Object[] comp = ((ObjArray) o).get();
        if (comp.length != objects.length) {
          return false;
        }
        for (int i = 0; i < comp.length; i++) {
          if (!comp[i].equals(objects[i])) {
            return false;
          }
        }
        return true;
      } else {
        return false;
      }
    }

    /**
     * get the internal object array.
     *
     * @return the internal object array.
     */
    Object[] get() {
      return objects;
    }
  }
}
