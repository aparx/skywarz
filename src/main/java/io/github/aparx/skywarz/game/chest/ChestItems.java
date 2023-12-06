package io.github.aparx.skywarz.game.chest;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.configurable.field.ConfigField;
import io.github.aparx.bufig.configurable.field.ConfigFieldValueMutator;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.material.Chest;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 12:08
 * @since 1.0
 */
@Getter
public class ChestItems implements ConfigurationSerializable {

  static {
    ConfigField.getMutators().add(ConfigFieldValueMutator.newMapper(
        ChestItems.class, Collection.class,
        (field, value) -> value.values(),
        (field, value) -> ChestItems.of(value)));
  }

  private final @NonNull NavigableMap<Double, ChestItem> map = new TreeMap<>();

  private double total;

  public static ChestItems deserialize(Map<String, Object> args) {
    Object object = args.get("data");
    if (!(object instanceof Collection))
      return new ChestItems();
    return of((Collection<?>) object);
  }

  private static ChestItems of(Collection<?> collection) {
    Preconditions.checkNotNull(collection, "Collection must not be null");
    ChestItems items = new ChestItems();
    for (Object o : collection)
      if (o instanceof ChestItem)
        items.add((ChestItem) o);
    return items;
  }

  @Override
  public @NonNull Map<String, Object> serialize() {
    return Map.of("data", values());
  }

  public void add(@NonNull ChestItem item) {
    Preconditions.checkNotNull(item, "Item must not be null");
    total += item.getWeight();
    map.put(total, item);
  }

  public void addAll(@NonNull Collection<? extends ChestItem> collection) {
    Preconditions.checkNotNull(collection, "Collection must not be null");
    collection.forEach(this::add);
  }

  public @NonNull ChestItem next() {
    return next(ThreadLocalRandom.current());
  }

  public @NonNull ChestItem next(@NonNull Random random) {
    Preconditions.checkNotNull(random, "Random must not be null");
    return map.ceilingEntry(random.nextDouble() * total).getValue();
  }

  public @NonNull Collection<ChestItem> values() {
    return new ArrayList<>(map.values());
  }
}
