package edu.brown.cs.jmrs.collect.graph;

/**
 * Represents an object which can be located in k-dimensional space. Provides
 * additional helper methods for such objects.
 *
 * @author mcisler
 *
 */
public interface Localizable {
  /**
   * Provides the coordinate value of this object along the given axis.
   *
   * @param axis
   *          A axes from 0 to getAxes()-1
   * @return This coordinate value in that axis
   */
  double getCoordinate(int axis);

  /**
   * Returns the number of axes this object has. (The dimensionality of space it
   * exists in)
   *
   * @return The number of axes (size of dimensional space) this object exists
   *         in
   */
  int getNumAxes();

  /**
   * @return Whether the Localizable object is valid, i.e. whether it has a
   *         positive dimensionality
   */
  default boolean isValid() {
    return getNumAxes() > 0; // number of axes must be 1 or more
  }

  /**
   * Provides the distance between two Localizables.
   *
   * @param other
   *          The other Localizable to measure the distance to
   * @return The distance between the Localizables
   */
  default double distance(Localizable other) {
    this.checkDimensionality(other);

    double squaredComponents = 0;

    for (int axis = 0; axis < getNumAxes(); axis++) {
      squaredComponents +=
          Math.pow(this.getCoordinate(axis) - other.getCoordinate(axis), 2);
    }

    return Math.sqrt(squaredComponents);
  }

  /**
   * Determines whether the two Localizables are of the same dimensionality.
   *
   * @param other
   *          The Localizable to compare to
   * @return Whether the two Localizables have the same dimensionality
   */
  default boolean sameDimensionality(Localizable other) {
    return this.getNumAxes() == other.getNumAxes();
  }

  /**
   * Determines whether the two Localizables are of the same dimensionality and
   * throws an error if they are not.
   *
   * @param other
   *          The Localizable to compare to
   */
  default void checkDimensionality(Localizable other) {
    if (!this.sameDimensionality(other)) {
      throw new IllegalArgumentException(
          "The provided Localizables do not have the same dimentionality");
    }
  }
}
