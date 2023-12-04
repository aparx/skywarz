package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.SpawnMap;
import io.github.aparx.skywarz.game.arena.snapshot.ArenaDataSnapshot;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
  private @NonNull Map<String, SpawnGroup> spawns;
  private @NonNull GameSettings settings;

  public ArenaData() {
    this(new ArenaBox(), new HashMap<>(), GameSettings.of());
  }

  public ArenaData(
      @NonNull ArenaBox box,
      @NonNull Map<String, SpawnGroup> spawns,
      @NonNull GameSettings settings) {
    Preconditions.checkNotNull(box, "Box must not be null");
    this.box = box;
    setSpawns(spawns);
    setSettings(settings);
  }

  @SuppressWarnings("unchecked")
  public static ArenaData deserialize(@NonNull Map<?, ?> data) {
    ArenaData newData = new ArenaData((ArenaBox) data.get("box"),
        (Map<String, SpawnGroup>) data.get("spawns"),
        (GameSettings) data.get("settings"));
    if (data.containsKey("world"))
      newData.setWorld(Bukkit.getWorld((String) data.get("world")));
    newData.setLobby((Location) data.get("lobby"));
    newData.setSpectator((Location) data.get("spectator"));
    return newData;
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();
    map.put("box", getBox());
    map.put("lobby", getLobby());
    map.put("spectator", getSpectator());
    if (this.world != null)
      map.put("world", getWorld().getName());
    map.put("spawns", getSpawns());
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

  public void setSettings(@NonNull GameSettings settings) {
    Preconditions.checkNotNull(settings, "Settings must not be null");
    this.settings = settings;
  }

  public void setSpawns(@NonNull Map<String, SpawnGroup> spawns) {
    Preconditions.checkNotNull(spawns, "Spawns must not be null");
    this.spawns = spawns;
  }

  @CanIgnoreReturnValue
  public SpawnGroup createSpawnsIfAbsent(@NonNull TeamEnum team) {
    return spawns.computeIfAbsent(team.name(), (key) -> new SpawnMap());
  }

}
