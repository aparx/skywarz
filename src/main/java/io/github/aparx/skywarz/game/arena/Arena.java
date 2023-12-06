package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.arena.reset.ArenaReset;
import io.github.aparx.skywarz.game.chest.ChestConfig;
import io.github.aparx.skywarz.game.chest.ChestHandler;
import io.github.aparx.skywarz.game.chest.ChestItems;
import io.github.aparx.skywarz.setup.CompletableSetup;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:38
 * @since 1.0
 */
@Getter
@SerializableAs("Skywarz.Arena")
public final class Arena implements ConfigurationSerializable, CompletableSetup {

  @Setter(AccessLevel.NONE)
  private final @NonNull String name;

  private final @NonNull ArenaData data;

  private final ArenaReset reset = new ArenaReset(this);

  public Arena(@NonNull String name) {
    this(name, new ArenaData());
  }

  private Arena(@NonNull String name, @NonNull ArenaData data) {
    Preconditions.checkState(StringUtils.isNotBlank(name), "Name must not be blank");
    Preconditions.checkNotNull(data, "Data must not be null");
    this.name = name;
    this.data = data;
  }

  public static Arena deserialize(@NonNull Map<?, ?> data) {
    return new Arena((String) data.get("name"), (ArenaData) data.get("data"));
  }

  @Override
  public boolean isCompleted() {
    return data.getBox().isCompleted()
        && data.getLobby() != null
        && data.getSpectator() != null
        && !data.getSpawns().isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Arena arena = (Arena) o;
    return Objects.equals(name, arena.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public Map<String, Object> serialize() {
    return Map.of("name", getName(), "data", getData());
  }
}
