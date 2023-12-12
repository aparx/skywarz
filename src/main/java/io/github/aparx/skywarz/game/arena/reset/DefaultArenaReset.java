package io.github.aparx.skywarz.game.arena.reset;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.utils.TimedProcedure;
import io.github.aparx.skywarz.utils.material.MaterialTag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 08:51
 * @since 1.0
 */
@Getter
public class DefaultArenaReset extends ArenaReset {

  /**
   * The amount of blocks the Arena's bounding box is expanded to clear items in that area.
   * <p>This is to ensure that if a player dies at the edge of the arena and drops items, those
   * items are actually cleared.
   */
  private static final double ITEM_CLEAR_BOX_EXPANSION = 2.0;

  private final Set<BlockSnapshot> blocks = Collections.synchronizedSet(new HashSet<>());

  private final DefaultArenaResetListener listener = new DefaultArenaResetListener(this);

  public DefaultArenaReset(@NonNull GameArena arena) {
    super(arena);
  }

  @Override
  public void capture() {
    blocks.clear();
    Bukkit.getPluginManager().registerEvents(listener, Skywars.plugin());
  }

  @Override
  public void reset() {
    HandlerList.unregisterAll(listener);
    new TimedProcedure().execute((time) -> {
      GameArena arena = getArena();
      Skywars.logger().log(Level.INFO, "Resetting {0} (...)", arena.getName());
      blocks.forEach((snapshot) -> {
        Location location = snapshot.getLocation();
        World world = location.getWorld();
        if (world != null)
          world.getBlockAt(location).setBlockData(snapshot.getBlockData());
      });
      blocks.clear();
      World world = arena.getData().getWorld();
      world.getNearbyEntities(arena.getData()
              .getBox().toBoundingBox()
              .expand(ITEM_CLEAR_BOX_EXPANSION))
          .stream()
          .filter((e) -> e instanceof Item)
          .forEach(Entity::remove);
      Skywars.logger().log(Level.INFO, "Reset of {0} completed in {1}",
          new Object[]{arena.getName(), time.toPerformanceString()});
    });
  }

  @CanIgnoreReturnValue
  public boolean addSnapshot(BlockSnapshot snapshot) {
    return blocks.add(snapshot);
  }

  @CanIgnoreReturnValue
  public boolean addStructure(BlockSnapshot snapshot) {
    return addStructure(snapshot, MaterialTag.connected);
  }

  @CanIgnoreReturnValue
  public boolean addStructure(BlockSnapshot snapshot, Predicate<Material> predicate) {
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
          if (predicate.test(blockAt.getType()))
            addStructure(BlockSnapshot.take(blockAt), predicate);
        }
      }
    }
    return true;
  }

  @Getter
  @RequiredArgsConstructor
  public static final class BlockSnapshot {
    private final @NonNull Location location;
    private final @NonNull BlockData blockData;

    public static BlockSnapshot take(@NonNull Block block) {
      Preconditions.checkNotNull(block, "Block must not be null");
      return new BlockSnapshot(block.getLocation(), block.getBlockData().clone());
    }

    public static BlockSnapshot take(@NonNull BlockState state) {
      Preconditions.checkNotNull(state, "Block must not be null");
      return new BlockSnapshot(state.getLocation(), state.getBlockData().clone());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      BlockSnapshot that = (BlockSnapshot) o;
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
