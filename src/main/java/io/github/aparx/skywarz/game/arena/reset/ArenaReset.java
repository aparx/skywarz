package io.github.aparx.skywarz.game.arena.reset;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.utils.material.ConnectedMaterial;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 08:51
 * @since 1.0
 */
@Getter
public class ArenaReset {

  private final WeakReference<Arena> arena;

  private final Set<LocationSnapshot> snapshots = Collections.synchronizedSet(new HashSet<>());

  private final String arenaName;

  public ArenaReset(@NonNull Arena arena) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    this.arena = new WeakReference<>(arena);
    this.arenaName = arena.getName();
  }

  public Optional<Arena> findArena() {
    return Optional.ofNullable(arena.get());
  }

  public @NonNull Arena getArena() {
    return findArena().orElseThrow();
  }

  public void reset() {
    Skywars.plugin().getLogger().log(Level.INFO, "Resetting arena {0}", arenaName);
    snapshots.forEach((snapshot) -> {
      Location location = snapshot.getLocation();
      World world = location.getWorld();
      if (world != null)
        world.getBlockAt(location).setBlockData(snapshot.getBlockData());
    });
    snapshots.clear();
    Arena arena = getArena();
    World world = arena.getData().getWorld();
    world.getNearbyEntities(arena.getData().getBox().toBoundingBox()).stream()
        .filter((e) -> e instanceof Item)
        .forEach(Entity::remove);
    Skywars.plugin().getLogger().info("Reset complete");
  }

  @CanIgnoreReturnValue
  public boolean addSnapshot(LocationSnapshot snapshot) {
    return snapshots.add(snapshot);
  }

  @CanIgnoreReturnValue
  public boolean addStructure(LocationSnapshot snapshot) {
    if (!addSnapshot(snapshot)) return false;
    Location location = snapshot.getLocation();
    World world = location.getWorld();
    if (world == null) return false;
    int posX = location.getBlockX();
    int posY = location.getBlockY();
    int posZ = location.getBlockZ();
    for (int x = -1; x <= 1; ++x) {
      for (int y = -1; y <= 1; ++y) {
        for (int z = -1; z <= 1; ++z) {
          Block blockAt = world.getBlockAt(posX + x, posY + y, posZ + z);
          if (ConnectedMaterial.isStructure(blockAt.getType())) {
            addStructure(new LocationSnapshot(blockAt.getLocation(),
                blockAt.getBlockData().clone()));
          }
        }
      }
    }
    return true;
  }

  @Getter
  @RequiredArgsConstructor
  public static final class LocationSnapshot {
    private final @NonNull Location location;
    private final @NonNull BlockData blockData;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      LocationSnapshot that = (LocationSnapshot) o;
      return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
      return Objects.hash(location);
    }

    @Override
    public String toString() {
      return "LocationSnapshot{" +
          "location=" + location +
          ", blockData=" + blockData +
          '}';
    }
  }

}
