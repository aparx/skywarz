package io.github.aparx.skywarz.game;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 08:28
 * @since 1.0
 */
public interface SpawnGroup {

  int size();

  boolean isEmpty();

  Optional<Location> find(int id);

  @NonNull Location get(int id);

  @Nullable Location set(int id, Location location);

  @CanIgnoreReturnValue
  int add(@NonNull Location location);

  @Nullable Location remove(int id);

  void clear();

  Stream<Map.Entry<Integer, Location>> stream();

  /** Returns a snapshot (shallow copy of this map) of the current group */
  SpawnGroup createSnapshot();

}
