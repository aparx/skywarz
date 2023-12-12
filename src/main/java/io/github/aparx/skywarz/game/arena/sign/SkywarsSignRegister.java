package io.github.aparx.skywarz.game.arena.sign;

import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import io.github.aparx.skywarz.utils.collection.KeyValueSets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-10 20:28
 * @since 1.0
 */
@Getter
@SerializableAs("Skywarz.SignRegister")
@RequiredArgsConstructor
public final class SkywarsSignRegister implements ConfigurationSerializable {

  private static final Function<ArenaSign, Location> KEY_MAPPER = ArenaSign::getLocation;

  private final @NonNull KeyValueSet<Location, ArenaSign> collection;

  public SkywarsSignRegister() {
    this(KeyValueSets.of(KEY_MAPPER));
  }

  public static SkywarsSignRegister deserialize(Map<?, ?> data) {
    Object dataObject = data.get("data");
    if (!(dataObject instanceof Collection<?>))
      dataObject = List.of();
    KeyValueSet<Location, ArenaSign> newSet = KeyValueSets.of(KEY_MAPPER);
    for (Object object : (Collection<?>) dataObject)
      if (object instanceof ArenaSign)
        newSet.add((ArenaSign) object);
    return new SkywarsSignRegister(newSet);
  }

  @Override
  public @NonNull Map<String, Object> serialize() {
    return Map.of("data", new ArrayList<>(collection));
  }

  @Override
  public String toString() {
    return "SkywarsSignRegister{" +
        "collection=" + collection +
        '}';
  }
}
