package io.github.aparx.skywarz.skywars.arena;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.setup.CompletableSetup;
import io.github.aparx.skywarz.skywars.team.TeamEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:38
 * @since 1.0
 */
@Setter
@Getter
@SerializableAs("Skywarz.Arena")
public final class Arena implements ConfigurationSerializable, CompletableSetup {

  @Setter(AccessLevel.NONE)
  private final @NonNull String name;

  @Setter(AccessLevel.NONE)
  private final @NonNull MutableArenaBox box;

  private @Nullable Location spectator;

  private @Nullable Location lobby;

  private @Nullable WeakReference<World> world;

  private Map<String, SpawnList> spawns = new HashMap<>();

  public Arena(@NonNull String name) {
    this(name, new MutableArenaBox());
  }

  private Arena(@NonNull String name, @NonNull MutableArenaBox box) {
    Preconditions.checkState(StringUtils.isNotBlank(name), "Name must not be blank");
    Preconditions.checkNotNull(box, "Box must not be null");
    this.name = name;
    this.box = box;
  }

  @SuppressWarnings("unchecked")
  public static Arena deserialize(@NonNull Map<?, ?> data) {
    Arena arena = new Arena((String) data.get("name"), (MutableArenaBox) data.get("box"));
    if (StringUtils.isNotEmpty((String) data.get("world")))
      arena.setWorld(Bukkit.getWorld((String) data.get("world")));
    arena.setSpectator((Location) data.get("spectator"));
    arena.setLobby((Location) data.get("lobby"));
    System.out.println(data.get("spawns"));
    arena.setSpawns((Map<String, SpawnList>) data.get("spawns"));
    return arena;
  }

  public void setWorld(@NonNull World world) {
    Preconditions.checkNotNull(world, "World must not be null");
    this.world = new WeakReference<>(world);
  }

  public @NonNull World getWorld() {
    Preconditions.checkNotNull(world, "World is not set");
    World referee = this.world.get();
    Preconditions.checkState(referee != null, "World has become invalid");
    return referee;
  }

  @Override
  public boolean isCompleted() {
    return spectator != null && world != null && box.isCompleted();
  }

  public Optional<SpawnList> getSpawns(@NonNull TeamEnum team) {
    Preconditions.checkNotNull(team, "Team must not be null");
    return Optional.ofNullable(getSpawns().get(team.name()));
  }

  public SpawnList createSpawnsIfAbsent(TeamEnum team) {
    return spawns.computeIfAbsent(team.name(), (ignored) -> new SpawnList());
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
    HashMap<String, Object> map = new HashMap<>();
    map.put("name", getName());
    map.put("box", getBox());
    if (world != null)
      map.put("world", getWorld().getName());
    map.put("spectator", getSpectator());
    map.put("lobby", getLobby());
    map.put("spawns", getSpawns());
    return map;
  }
}
