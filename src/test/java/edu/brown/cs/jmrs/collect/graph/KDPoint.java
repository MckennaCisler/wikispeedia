package edu.brown.cs.jmrs.collect.graph;

import java.util.Objects;

/**
 * A class to contain locations in k-dimensional space.
 *
 * @author mcisler
 *
 */
public class KDPoint implements Localizable {
  private final int numAxes;
  private final double[] coords;

  /**
   * Constructs a point in k-dimensional space.
   *
   * @param coords
   *          An array of all coordinates representing this point
   */
  public KDPoint(double... coords) {
    super();
    if (coords.length <= 0) {
      throw new IllegalArgumentException(
          "A KDPoint has a minimum dimensionality of 1");
    }

    this.coords = coords;
    this.numAxes = coords.length;
  }

  /**
   * {@link edu.brown.cs.mcisler.kdspace.Localizable#getCoordinate(int)}.
   */
  @Override
  public double getCoordinate(int axis) {
    if (axis >= numAxes) {
      throw new IllegalArgumentException(
          "The provided axis was beyond the available space");
    }

    return coords[axis];
  }

  /**
   * {@link edu.brown.cs.mcisler.kdspace.Localizable#getNumAxes()}.
   */
  @Override
  public int getNumAxes() {
    return numAxes;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("(");

    for (int i = 0; i < numAxes; i++) {
      str.append(String.format("%1.1f, ", getCoordinate(i)));
    }

    // remove last comma and space
    str.delete(str.length() - 2, str.length());
    str.append(")");

    return str.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null) {
      return false;
    }

    if (!(o instanceof KDPoint)) {
      return false;
    }

    KDPoint other = (KDPoint) o;

    if (other.getNumAxes() != this.getNumAxes()) {
      return false;
    }

    for (int i = 0; i < numAxes; i++) {
      if (Double.doubleToLongBits(other.getCoordinate(i)) != Double
          .doubleToLongBits(this.getCoordinate(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(coords, numAxes);
  }
}
