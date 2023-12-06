package io.github.aparx.skywarz.game.chest;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.game.arena.Arena;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 11:59
 * @since 1.0
 */
@Getter
public class ChestHandler {

  @Getter(AccessLevel.NONE)
  private final WeakReference<Arena> arena;

  @Getter(AccessLevel.NONE)
  private final Set<Vector> opened = new HashSet<>();

  private final @NonNull ChestItems items;

  public ChestHandler(@NonNull Arena arena, @NonNull ChestItems items) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    this.arena = new WeakReference<>(arena);
    this.items = items;
  }

  public void reset() {
    try {
      findArena().ifPresent((arena) -> {
        World world = arena.getData().getWorld();
        opened.forEach((vector) -> {
          Block block = vector.toLocation(world).getBlock();
          if (!(block.getState() instanceof Chest)) return;
          Chest chest = (Chest) block.getState();
          chest.getInventory().clear();
        });
      });
    } finally {
      opened.clear();
    }
  }

  public Optional<Arena> findArena() {
    return Optional.ofNullable(arena.get());
  }

  public @NonNull Arena getArena() {
    return findArena().orElseThrow();
  }

  @CanIgnoreReturnValue
  public boolean fill(@NonNull Location location, @NonNull Inventory inventory) {
    if (!markOpened(location)) return false;
    inventory.clear();
    int probability = ChestConfig.getInstance().getProbability();
    for (int i = 0, n = inventory.getSize(); i < n; ++i)
      if (ThreadLocalRandom.current().nextInt(0, 100) <= probability) {
        ItemStack stack = items.next().getStack();
        if (stack.getAmount() > 1) {
          stack = stack.clone();
          stack.setAmount(ThreadLocalRandom.current().nextInt(1, stack.getAmount()));
        }
        inventory.setItem(i, stack);
      }
    return true;
  }

  @CanIgnoreReturnValue
  public boolean markOpened(@NonNull Location location) {
    Preconditions.checkNotNull(location, "Location must not be null");
    if (!Objects.equals(location.getWorld(), getArena().getData().getWorld()))
      return false;
    return opened.add(location.toVector());
  }

  public boolean isOpened(@NonNull Location location) {
    if (Objects.equals(location.getWorld(), getArena().getData().getWorld()))
      return opened.contains(location.toVector());
    return false;
  }

  public boolean isOpened(@NonNull Vector vector) {
    return opened.contains(vector);
  }

}
