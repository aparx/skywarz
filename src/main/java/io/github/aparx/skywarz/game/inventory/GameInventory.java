package io.github.aparx.skywarz.game.inventory;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.utils.collection.WeakHashSet;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import lombok.Getter;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 13:03
 * @since 1.0
 */
@Getter
public class GameInventory implements Listener {

  private final @Nullable InventoryHolder holder;

  private final @NonNull InventoryContent content;

  private final @NonNull TickDuration updateInterval;

  private final Inventory inventory;

  private volatile BukkitTask task;

  private final TimeTicker updateTicker = new TimeTicker();

  private final WeakHashSet<HumanEntity> viewers = new WeakHashSet<>();

  public GameInventory(
      @Nullable InventoryHolder holder,
      @NonNull InventoryDimensions dimensions,
      @NonNull TickDuration updateInterval,
      @NonNull String title) {
    this(holder, new InventoryContent(dimensions), updateInterval, title);
  }

  public GameInventory(
      @Nullable InventoryHolder holder,
      @NonNull InventoryContent content,
      @NonNull TickDuration updateInterval,
      @NonNull String title) {
    Preconditions.checkNotNull(content, "Content must not be null");
    Preconditions.checkNotNull(updateInterval, "Interval must not be null");
    this.holder = holder;
    this.inventory = Bukkit.createInventory(holder, content.getDimensions().size(), title);
    this.content = content;
    this.updateInterval = updateInterval;
  }

  public final InventoryDimensions getDimensions() {
    return getContent().getDimensions();
  }

  @Synchronized
  public void open(Player player) {
    player.openInventory(inventory);
    if (!viewers.add(player)) return;
    start(getUpdateInterval());
    updateInventory();
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean close(Player player) {
    if (!viewers.remove(player)) return false;
    player.closeInventory();
    return true;
  }

  public void updateInventory() {
    updateInventory(getUpdateTicker().tick());
  }

  @Synchronized
  public void updateInventory(long ticks) {
    if (viewers.isEmpty()) stop();
    InventoryContent content = getContent();
    getDimensions().stream().forEach((index) -> {
      inventory.setItem(index, content.find(index).map((x) -> x.get(ticks)).orElse(null));
    });
  }

  @Synchronized
  @CanIgnoreReturnValue
  protected boolean start(TickDuration interval) {
    if (task != null) return false;
    Bukkit.getPluginManager().registerEvents(this, Skywars.plugin());
    task = Bukkit.getScheduler().runTaskTimer(Skywars.plugin(),
        () -> updateInventory(updateTicker.tick()),
        interval.toTicks(), interval.toTicks());
    return true;
  }

  @Synchronized
  @CanIgnoreReturnValue
  protected boolean stop() {
    if (task == null) return false;
    HandlerList.unregisterAll(this);
    viewers.clear();
    task.cancel();
    task = null;
    return true;
  }

  @Synchronized
  @EventHandler
  void onInventoryClick(InventoryClickEvent event) {
    HumanEntity whoClicked = event.getWhoClicked();
    if (!(whoClicked instanceof Player)) return;
    if (!viewers.contains(whoClicked)) return;
    int slot = event.getSlot();
    if (event.getCurrentItem() != null
        && slot >= 0 && slot < getDimensions().size()) {
      SkywarsPlayer player = SkywarsPlayer.getPlayer((Player) whoClicked);
      content.find(slot).ifPresent((item) -> item.click(player, event));
    }
  }

  @Synchronized
  @EventHandler
  void onInventoryClose(InventoryCloseEvent event) {
    close((Player) event.getPlayer());
  }

}