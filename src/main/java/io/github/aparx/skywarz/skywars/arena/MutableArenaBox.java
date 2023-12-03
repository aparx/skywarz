package io.github.aparx.skywarz.skywars.arena;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.github.aparx.skywarz.setup.CompletableSetup;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
@SerializableAs("Skywarz.MutableArenaBox")
public final class MutableArenaBox implements ConfigurationSerializable, CompletableSetup {

  private final @Nullable Vector @NonNull [] points;

  public MutableArenaBox() {
    this(new Vector[Point.CONSTANTS.size()]);
  }

  private MutableArenaBox(@Nullable Vector @NonNull [] points) {
    Preconditions.checkArgument(
        ArrayUtils.getLength(points) == Point.CONSTANTS.size(),
        "Given points array length is not equal to what is required");
    this.points = points;
  }

  @SuppressWarnings("unchecked")
  public static MutableArenaBox deserialize(@NonNull Map<?, ?> data) {
    return new MutableArenaBox(((Collection<Vector>) data.get("points"))
        .toArray(new Vector[0]));
  }

  @Override
  public boolean isCompleted() {
    return !ArrayUtils.contains(points, null);
  }

  public void setPoint(@NonNull Point point, Vector vector) {
    Preconditions.checkNotNull(point, "Corner must not be null");
    points[point.ordinal()] = vector;
    if (isCompleted()) sortMinAndMaxCoordinates();
  }

  public Optional<Vector> getPoint(@NonNull Point point) {
    Preconditions.checkNotNull(point, "Corner must not be null");
    return Optional.ofNullable(points[point.ordinal()]);
  }

  public boolean isWithin(@NonNull Vector position) {
    Preconditions.checkNotNull(position, "Position must not be null");
    return position.isInAABB(points[Point.MIN.ordinal()], points[Point.MAX.ordinal()]);
  }

  private void sortMinAndMaxCoordinates() {
    Vector min = points[Point.MIN.ordinal()];
    Vector max = points[Point.MAX.ordinal()];
    Preconditions.checkNotNull(min, "Min point is null");
    Preconditions.checkNotNull(max, "Max point is null");
    points[Point.MIN.ordinal()] = Vector.getMinimum(min, max);
    points[Point.MAX.ordinal()] = Vector.getMaximum(min, max);
  }

  @Override
  public Map<String, Object> serialize() {
    return Map.of("points", points);
  }

  public enum Point {
    MIN, MAX;

    public static final ImmutableList<Point> CONSTANTS = ImmutableList.copyOf(values());

    public static @NonNull Point ofIndex(int index) {
      Preconditions.checkElementIndex(index, CONSTANTS.size());
      return CONSTANTS.get(index);
    }
  }

}
