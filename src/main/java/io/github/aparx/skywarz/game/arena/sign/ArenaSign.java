package io.github.aparx.skywarz.game.arena.sign;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.LazyVariableLookup;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-10 17:36
 * @since 1.0
 */
@SerializableAs("Skywarz.Sign")
public class ArenaSign implements ConfigurationSerializable {

  private final @NonNull Location location;

  public ArenaSign(@NonNull Location location) {
    Preconditions.checkNotNull(location, "Location must not be null");
    this.location = location;
  }

  public ArenaSign(@NonNull BlockState state) {
    this(state.getLocation());
    Preconditions.checkState(state instanceof Sign, "State not a sign");
  }

  public static ArenaSign deserialize(Map<?, ?> data) {
    return new ArenaSign((Location) data.get("location"));
  }

  @Override
  public @NonNull Map<String, Object> serialize() {
    return Map.of("location", getLocation());
  }

  @CanIgnoreReturnValue
  public boolean update(@NonNull LazyVariableLookup lookup, GameArena arena) {
    Block block = location.getBlock();
    BlockState state = block.getState();
    if (!(state instanceof Sign))
      return false;
    List<String> list = arena.getSignHandler().getTemplate().stream()
        .map((line) -> Language.getInstance().substitute(line, lookup))
        .collect(Collectors.toList());
    ((Sign) state).setEditable(false);
    IntStream.range(0, list.size()).forEach((i) -> {
      ((Sign) state).setLine(i, list.get(i));
    });
    state.update(true);
    return true;
  }

  public @NonNull Location getLocation() {
    return location;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    ArenaSign that = (ArenaSign) object;
    return Objects.equals(location, that.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(location);
  }
}
