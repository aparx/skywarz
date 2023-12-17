package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.bufig.utils.ConversionUtils;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.SpawnList;
import io.github.aparx.skywarz.game.arena.settings.ArenaSettings;
import io.github.aparx.skywarz.game.team.TeamEnum;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 09:05
 * @since 1.0
 */
@Setter
@Getter
@SerializableAs("Skywarz.ArenaData")
public class ArenaData implements IArenaData, ConfigurationSerializable {

  private final @NonNull ArenaBox box;
  private @Nullable Location spectator;
  private @Nullable Location lobby;
  private @Nullable WeakReference<World> world;
  private @NonNull EnumMap<TeamEnum, SpawnGroup> spawns;
  private @NonNull ArenaSettings settings;

  public ArenaData() {
    this(new ArenaBox(), new EnumMap<>(TeamEnum.class), ArenaSettings.of());
  }

  public ArenaData(
      @NonNull ArenaBox box,
      @NonNull EnumMap<TeamEnum, SpawnGroup> spawns,
      @NonNull ArenaSettings settings) {
    Preconditions.checkNotNull(box, "Box must not be null");
    this.box = box;
    setSpawns(spawns);
    setSettings(settings);
  }

  public static ArenaData deserialize(@NonNull Map<?, ?> data) {
    ArenaData newData = new ArenaData((ArenaBox) data.get("box"),
        ConversionUtils.toEnumMap(ConversionUtils.toSpecificStringMap(data.get("spawns"),
            SpawnGroup.class::cast), TeamEnum.class),
        (ArenaSettings) data.get("settings"));
    newData.setLobby((Location) data.get("lobby"));
    newData.setSpectator((Location) data.get("spectator"));
    if (data.containsKey("world")) {
      World world = Bukkit.getWorld((String) data.get("world"));
      Preconditions.checkNotNull(world, "World of arena has become invalid");
      newData.setWorld(world);
    }
    return newData;
  }

  @Override
  public @NonNull Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();
    map.put("box", getBox());
    map.put("lobby", getLobby());
    map.put("spectator", getSpectator());
    if (this.world != null)
      map.put("world", getWorld().getName());
    map.put("spawns", ConversionUtils.toGenericStringMap(getSpawns()));
    map.put("settings", getSettings());
    return map;
  }

  @Override
  public boolean isCompleted() {
    return IArenaData.super.isCompleted() && world != null && world.get() != null;
  }

  public @NonNull World getWorld() {
    return IArenaData.getWorldFromReference(world);
  }

  public void setWorld(@NonNull World world) {
    Preconditions.checkNotNull(world, "World must not be null");
    this.world = new WeakReference<>(world);
  }

  public void setSettings(@NonNull ArenaSettings settings) {
    Preconditions.checkNotNull(settings, "Settings must not be null");
    this.settings = settings;
  }

  public void setSpawns(@NonNull EnumMap<TeamEnum, SpawnGroup> spawns) {
    Preconditions.checkNotNull(spawns, "Spawns must not be null");
    this.spawns = spawns;
  }

  @CanIgnoreReturnValue
  public SpawnGroup createSpawnsIfAbsent(@NonNull TeamEnum team) {
    return spawns.computeIfAbsent(team, (key) -> new SpawnList());
  }

}
