package edu.brown.cs.jmrs.wikispeedia;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.brown.cs.jmrs.ui.Main;
import edu.brown.cs.jmrs.web.wikipedia.WikiPage;
import edu.brown.cs.jmrs.wikispeedia.WikiPath.Visit;

/**
 * A simple path abstraction for wiki player's games, primarily to store times
 * of arrival.
 *
 * @author mcisler
 *
 */
public class WikiPath extends ArrayList<Visit> {
  /**
   * To implement serializable.
   */
  private static final long serialVersionUID = -6506229198670657490L;

  private Instant startTime;

  /**
   * Constructs an empty WikiPath.
   *
   * @param startPage
   *          The initial page, to add before starting.
   */
  public WikiPath(WikiPage startPage) {
    super();
    startTime = null;
    add(new Visit(startPage, 0));
  }

  /**
   * @param startTime
   *          The time the game started, for calculating relative arrival times.
   */
  public void setStartTime(Instant startTime) {
    assert startTime != null;
    this.startTime = startTime;
  }

  /**
   * @return The last page added.
   */
  public WikiPage end() {
    return get(size() - 1).getPage();
  }

  /**
   * @param page
   *          The page to check for.
   * @return Whether page is in this path.
   */
  public boolean contains(WikiPage page) {
    for (Visit visit : this) {
      // try to do deep equality, but if it fails revert to shallow
      if (visit.getPage().equalsAfterRedirectSafe(page)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Add page to this path, noting the current time.
   *
   * @param page
   *          The page to add.
   * @return True if successful.
   */
  public boolean add(WikiPage page) {
    if (startTime == null) {
      throw new IllegalStateException("Start time not specified");
    }
    return add(new Visit(page));
  }

  /**
   * A class for each page, storing arrival time and the page.
   *
   * @author mcisler
   *
   */
  public class Visit {
    private final long arrivalTime;
    private final WikiPage page;

    /**
     * @param arrivalTime
     *          The time page was arrived at.
     * @param page
     *          The page in this visit.
     */
    Visit(WikiPage page) {
      super();
      this.arrivalTime = Duration.between(startTime, Instant.now()).toMillis();
      this.page = page;
    }

    /**
     * @param arrivalTime
     *          The time page was arrived at.
     * @param page
     *          The page in this visit.
     * @param arrivalTime
     *          A specified arrival time.
     */
    Visit(WikiPage page, long arrivalTime) {
      super();
      this.arrivalTime = arrivalTime;
      this.page = page;
    }

    /**
     * @return The time page was arrived at.
     */
    public final long getArrivalTime() {
      return arrivalTime;
    }

    /**
     * @return The page in this visit.
     */
    public final WikiPage getPage() {
      return page;
    }

    @Override
    public int hashCode() {
      return Objects.hash(arrivalTime, page);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof Visit)) {
        return false;
      }
      Visit other = (Visit) obj;
      return other.arrivalTime == arrivalTime && other.page.equals(page);
    }

    @Override
    public String toString() {
      return String.format("%s at %s", page, arrivalTime);
    }
  }

  /**
   * Custom serializer for use with GSON. (On VISIT)
   *
   * @author mcisler
   *
   */
  public static class VisitSerializer implements JsonSerializer<Visit> {
    @Override
    public JsonElement serialize(Visit src, Type arg1,
        JsonSerializationContext arg2) {
      JsonObject root = new JsonObject();
      root.addProperty("arrivalTime", src.getArrivalTime());
      root.add("page", Main.GSON.toJsonTree(src.getPage()));
      return root;
    }
  }
}
