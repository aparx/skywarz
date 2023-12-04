package io.github.aparx.skywarz.game;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 03:41
 * @since 1.0
 */
@SerializableAs("Skywarz.SpawnList")
public class SpawnMap implements SpawnGroup, ConfigurationSerializable {
  private final @NonNull Map<Integer, Location> map;

  private final AtomicInteger elementCount;

  public SpawnMap() {
    this(new ConcurrentHashMap<>());
  }

  public SpawnMap(@NonNull Map<Integer, Location> map) {
    Preconditions.checkNotNull(map, "Map must not be null");
    this.map = map;
    this.elementCount = new AtomicInteger(map.size());
  }

  public static SpawnMap deserialize(Map<?, ?> data) {
    ConcurrentHashMap<Integer, Location> map = new ConcurrentHashMap<>(data.size());
    for (Map.Entry<?, ?> entry : data.entrySet())
      if (entry.getValue() instanceof Location)
        map.put(NumberConversions.toInt(entry.getKey()), (Location) entry.getValue());
    return new SpawnMap(map);
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> dataMap = new HashMap<>(map.size());
    for (Map.Entry<Integer, Location> entry : this.map.entrySet())
      dataMap.put(entry.getKey().toString(), entry.getValue());
    return dataMap;
  }

  @Override
  public int size() {
    int elementCount = this.elementCount.get();
    if (map.size() != elementCount)
      throw new ConcurrentModificationException();
    return elementCount;
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public Optional<Location> find(int id) {
    return Optional.ofNullable(map.get(id));
  }

  @Override
  public @NonNull Location get(int id) {
    return find(id).orElseThrow();
  }

  @Override
  @CanIgnoreReturnValue
  public @Nullable Location set(int id, Location location) {
    Preconditions.checkNotNull(location, "Location must not be null");
    Location previousLocation = map.put(id, location);
    if (previousLocation == null)
      elementCount.incrementAndGet();
    return previousLocation;
  }

  @Override
  @CanIgnoreReturnValue
  public int add(@NonNull Location location) {
    Preconditions.checkNotNull(location, "Location must not be null");
    int id = elementCount.incrementAndGet();
    map.put(id, location);
    return id;
  }

  @Override
  @CanIgnoreReturnValue
  public @Nullable Location remove(int id) {
    Location previousLocation = map.remove(id);
    if (previousLocation != null)
      elementCount.decrementAndGet();
    return previousLocation;
  }

  @Override
  public void clear() {
    elementCount.set(0);
    map.clear();
  }

  @Override
  public Stream<Map.Entry<Integer, Location>> stream() {
    return map.entrySet().stream();
  }

  @Override
  public SpawnGroup createSnapshot() {
    return new SpawnMap(new HashMap<>(map));
  }

}
