package io.github.aparx.skywarz.game.chest;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.handler.MainConfig;
import io.github.aparx.skywarz.language.VariablePopulator;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 11:59
 * @since 1.0
 */
@Getter
public class ChestHandler {

  public static final ChatColor HOLOGRAM_COLOR = ChatColor.GREEN;

  public static final Pattern HOLOGRAM_NAME_PATTERN =
      Pattern.compile(HOLOGRAM_COLOR + "([0-9][0-9](:?))+");

  @Getter(AccessLevel.NONE)
  private final WeakReference<GameArena> arena;

  @Getter(AccessLevel.NONE)
  private final Map<Location, OpenedChest> opened = new HashMap<>();

  private final @NonNull ChestItems items;

  private final TickDuration refillDelay;

  public ChestHandler(@NonNull GameArena arena, @NonNull ChestItems items) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    this.arena = new WeakReference<>(arena);
    this.items = items;
    this.refillDelay = MainConfig.getInstance().getDurationRefill();
  }

  public void reset() {
    try {
      findArena().ifPresent((arena) -> opened.forEach((location, chest) -> chest.reset()));
    } finally {
      opened.clear();
    }
  }

  public Optional<GameArena> findArena() {
    return Optional.ofNullable(arena.get());
  }

  public @NonNull GameArena getArena() {
    return findArena().orElseThrow();
  }

  @CanIgnoreReturnValue
  public boolean open(@NonNull Location location, @NonNull Inventory inventory) {
    Preconditions.checkNotNull(location, "Location must not be null");
    if (!Objects.equals(location.getWorld(), getArena().getData().getWorld()))
      return false;
    if (opened.containsKey(location))
      return false;
    OpenedChest chest = new OpenedChest(location);
    opened.put(location, chest);
    chest.fill(inventory);
    return true;
  }

  public boolean isOpened(@NonNull Location location) {
    return opened.containsKey(location);
  }

  public boolean isOpened(@NonNull Vector vector) {
    return opened.containsKey(vector.toLocation(getArena().getData().getWorld()));
  }

  @Getter
  @Setter
  @RequiredArgsConstructor
  private class OpenedChest {
    private final @NonNull Location location;
    private @Nullable ArmorStand armorStand;
    private @Nullable BukkitTask bukkitTask;

    public void fill(@NonNull Inventory inventory) {
      this.reset();
      GameArena arena = getArena();
      int probability = ChestConfig.getInstance().getProbability();
      for (int i = 0, n = inventory.getSize(); i < n; ++i)
        if (ThreadLocalRandom.current().nextInt(0, 100) <= probability) {
          ItemStack stack = items.next().getStack();
          int newAmount = stack.getAmount();
          if (stack.getAmount() > 1)
            newAmount = ThreadLocalRandom.current().nextInt(1, stack.getAmount());
          if (newAmount != stack.getAmount())
            (stack = stack.clone()).setAmount(newAmount);
          inventory.setItem(i, stack);
        }
      if (arena.getData().getSettings().getChestRefill()) {
        armorStand = spawnHologram();
        bukkitTask = createRefillUpdateTask();
      }
    }

    public void reset() {
      if (bukkitTask != null)
        bukkitTask.cancel();
      if (armorStand != null)
        armorStand.remove();
      armorStand = null;
      bukkitTask = null;
      Optional.ofNullable(getChest()).ifPresent((chest) -> {
        chest.getBlockInventory().clear();
      });
    }

    public @Nullable Chest getChest() {
      BlockState state = location.getBlock().getState();
      if (state instanceof Chest)
        return (Chest) state;
      return null;
    }

    private BukkitTask createRefillUpdateTask() {
      TimeTicker ticker = new TimeTicker(TimeUnit.SECONDS);
      return Bukkit.getScheduler().runTaskTimer(Skywars.plugin(), () -> {
        Chest chest = getChest();
        if (chest == null) {
          reset();
          opened.remove(getLocation());
        } else if (armorStand != null && armorStand.isValid()) {
          // create a new dummy ticker to invert this ticker to represent time left
          TimeTicker left = new TimeTicker(TimeUnit.TICKS);
          left.set(refillDelay.toTicks() - ticker.getElapsed(TimeUnit.TICKS));
          armorStand.setCustomName(HOLOGRAM_COLOR + VariablePopulator.formatRelativeDate(left));
        } else if (ticker.hasElapsed(refillDelay)) {
          fill(chest.getBlockInventory());
          Location center = getHorizontalCenter().add(0, 1.05, 0);
          World world = Objects.requireNonNull(location.getWorld());
          world.spawnParticle(Particle.FLAME, center, 3, 0, 0, 0, 0, null);
          world.playSound(center, Sound.BLOCK_NOTE_BLOCK_PLING, .1f, 1.25f);
        }
        ticker.tick();
      }, 0, ticker.getInterval().toTicks());
    }

    private Location getHorizontalCenter() {
      return location.clone().add(0.5, 0, 0.5);
    }

    private ArmorStand spawnHologram() {
      World world = location.getWorld();
      Preconditions.checkNotNull(world);
      Location offset = getHorizontalCenter().subtract(0, 1.01, 0);
      return world.spawn(offset, ArmorStand.class, (stand) -> {
        stand.setCustomNameVisible(true);
        stand.setInvulnerable(true);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCollidable(false);
      });
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) return true;
      if (object == null || getClass() != object.getClass()) return false;
      OpenedChest that = (OpenedChest) object;
      return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
      return Objects.hash(location);
    }
  }

}
