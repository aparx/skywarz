package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.setup.CompletableSetup;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Cuboid shape representing the box in which an {@code Arena} is played out.
 *
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:38
 * @since 1.0
 */
@SerializableAs("Skywarz.ArenaBox")
public final class ArenaBox implements ConfigurationSerializable, CompletableSetup {

  private final Vector @NonNull [] points;

  private final Vector @NonNull [] minmax;

  public ArenaBox() {
    this(new Vector[Point.values().length]);
  }

  private ArenaBox(@Nullable Vector @NonNull [] points) {
    Preconditions.checkArgument(
        ArrayUtils.getLength(points) == Point.values().length,
        "Given points array length is not equal to what is required");
    this.points = points;
    this.minmax = new Vector[points.length];
    if (isCompleted()) recalculateMinMax();
  }

  @SuppressWarnings("unchecked")
  public static ArenaBox deserialize(@NonNull Map<?, ?> data) {
    return new ArenaBox(((Collection<Vector>) data.get("points")).toArray(new Vector[0]));
  }

  @Override
  public boolean isCompleted() {
    return !ArrayUtils.contains(points, null);
  }

  public void recalculateMinMax() {
    Preconditions.checkState(isCompleted());
    Vector minVector = points[Point.MIN.ordinal()];
    Vector maxVector = points[Point.MAX.ordinal()];
    minmax[Point.MIN.ordinal()] = Vector.getMinimum(minVector, maxVector);
    minmax[Point.MAX.ordinal()] = Vector.getMaximum(minVector, maxVector);
  }

  public void setPoint(@NonNull Point point, Vector vector) {
    Preconditions.checkNotNull(point, "Corner must not be null");
    points[point.ordinal()] = vector;
    if (isCompleted()) recalculateMinMax();
  }

  public Optional<Vector> getPoint(@NonNull Point point) {
    Preconditions.checkNotNull(point, "Corner must not be null");
    return Optional.ofNullable(points[point.ordinal()]);
  }

  public boolean isWithin(@NonNull Vector position) {
    Preconditions.checkNotNull(position, "Position must not be null");
    return position.isInAABB(points[Point.MIN.ordinal()], points[Point.MAX.ordinal()]);
  }

  public boolean isWithin(@NonNull Location location) {
    Preconditions.checkNotNull(location, "Location must not be null");
    Preconditions.checkState(isCompleted(), "Box must be completed");
    Vector min = minmax[Point.MIN.ordinal()];
    Vector max = minmax[Point.MAX.ordinal()];
    Preconditions.checkNotNull(min);
    Preconditions.checkNotNull(max);
    return location.getBlockX() >= min.getBlockX()
        && location.getBlockZ() >= min.getBlockZ()
        && location.getBlockY() >= min.getBlockY()
        && location.getBlockX() <= max.getBlockX()
        && location.getBlockZ() <= max.getBlockZ()
        && location.getBlockY() <= max.getBlockY();
  }

  public boolean isWithinHorizontally(@NonNull Location location) {
    Preconditions.checkNotNull(location, "Location must not be null");
    Preconditions.checkState(isCompleted(), "Box must be completed");
    Vector min = minmax[Point.MIN.ordinal()];
    Vector max = minmax[Point.MAX.ordinal()];
    Preconditions.checkNotNull(min);
    Preconditions.checkNotNull(max);
    return location.getBlockX() >= min.getBlockX()
        && location.getBlockZ() >= min.getBlockZ()
        && location.getBlockX() <= max.getBlockX()
        && location.getBlockZ() <= max.getBlockZ();
  }

  public boolean isWithin(@NonNull BoundingBox boundingBox) {
    Preconditions.checkNotNull(boundingBox, "BoundingBox must not be null");
    Preconditions.checkState(isCompleted(), "Box must be completed");
    Vector min = minmax[Point.MIN.ordinal()];
    Vector max = minmax[Point.MAX.ordinal()];
    return boundingBox.getMinX() >= min.getBlockX()
        && boundingBox.getMinZ() >= min.getBlockZ()
        && boundingBox.getMinY() >= min.getBlockY()
        && boundingBox.getMaxX() <= max.getBlockX()
        && boundingBox.getMaxZ() <= max.getBlockZ()
        && boundingBox.getMaxY() <= max.getBlockY();
  }

  public BoundingBox toBoundingBox() {
    Preconditions.checkState(isCompleted(), "Box must be completed");
    Vector min = points[Point.MIN.ordinal()];
    Vector max = points[Point.MAX.ordinal()];
    return new BoundingBox(
        min.getX(), min.getY(), min.getZ(),
        max.getX(), max.getY(), max.getZ());
  }

  @Override
  public @NonNull Map<String, Object> serialize() {
    return Map.of("points", points);
  }

  public enum Point {
    MIN, MAX;

    public Point other() {
      Point[] points = Point.values();
      return points[ordinal() % points.length];
    }

    public static @NonNull Point ofIndex(int index) {
      final Point[] values = values();
      Preconditions.checkElementIndex(index, values.length);
      return values[index];
    }
  }

}
