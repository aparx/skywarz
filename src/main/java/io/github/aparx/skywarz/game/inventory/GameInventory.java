package io.github.aparx.skywarz.game.inventory;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.game.inventory.content.InventoryContentView;
import io.github.aparx.skywarz.utils.collection.WeakHashSet;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import lombok.Getter;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 13:03
 * @since 1.0
 */
@Getter
public class GameInventory<T extends InventoryContentView> implements Listener {

  private final @Nullable InventoryHolder holder;

  private final @NonNull TickDuration updateInterval;

  private final Function<GameInventory<T>, String> titleFactory;

  private T content;

  private Inventory inventory;

  private volatile BukkitTask task;

  private final TimeTicker updateTicker = new TimeTicker();

  private final WeakHashSet<HumanEntity> viewers = new WeakHashSet<>();

  public GameInventory(
      @Nullable InventoryHolder holder,
      @NonNull TickDuration updateInterval,
      @NonNull String title,
      @Nullable T content) {
    this(holder, updateInterval, (that) -> title, content);
  }

  public GameInventory(
      @Nullable InventoryHolder holder,
      @NonNull TickDuration updateInterval,
      @NonNull Function<GameInventory<T>, String> titleFactory,
      @Nullable T content) {
    Preconditions.checkNotNull(updateInterval, "Interval must not be null");
    Preconditions.checkNotNull(titleFactory, "Title factory must not be null");
    this.holder = holder;
    this.content = content;
    this.titleFactory = titleFactory;
    this.updateInterval = updateInterval;
    if (content != null)
      recreateInventory();
  }

  public static <T extends InventoryContentView> GameInventory<T> createInventory(
      @NonNull TickDuration updateInterval,
      @NonNull Function<GameInventory<T>, T> contentFactory,
      @NonNull Function<GameInventory<T>, String> titleFactory) {
    GameInventory<T> inventory = new GameInventory<>(null, updateInterval, titleFactory, null);
    inventory.setContent(contentFactory.apply(inventory));
    return inventory;
  }

  public static <T extends InventoryContentView> GameInventory<T> createInventory(
      @NonNull TickDuration updateInterval,
      @NonNull Function<GameInventory<T>, T> contentFactory,
      @NonNull String title) {
    return createInventory(updateInterval, contentFactory, (x) -> title);
  }

  @Synchronized
  public final InventoryDimensions getDimensions() {
    Preconditions.checkNotNull(content, "Content is not defined");
    return content.getDimensions();
  }

  @Synchronized
  public void setContent(@NonNull T content) {
    Preconditions.checkNotNull(content, "Content must not be null");
    this.content = content;
    if (!content.getDimensions().equals(getDimensions()))
      recreateInventory();
  }

  @Synchronized
  public void recreateInventory() {
    Preconditions.checkNotNull(content, "Content is not defined");
    this.inventory = Bukkit.createInventory(holder,
        getDimensions().size(),
        titleFactory.apply(this));
    if (!viewers.isEmpty())
      viewers.forEach((viewer) -> viewer.openInventory(inventory));
  }

  @Synchronized
  public void open(Player player) {
    if (inventory == null)
      recreateInventory();
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
    if (content == null) return;
    InventoryContentView content = getContent();
    getDimensions().stream().forEach((index) -> {
      ItemStack newItem = content.find(index)
          .map((item) -> item.get(ticks))
          .orElse(null);
      if (!WrappedItemStack.isSimilar(newItem, inventory.getItem(index)))
        inventory.setItem(index, newItem);
    });
  }

  @Synchronized
  @CanIgnoreReturnValue
  protected boolean start(TickDuration interval) {
    if (task != null) return false;
    task = Bukkit.getScheduler().runTaskTimer(Skywars.plugin(),
        () -> updateInventory(updateTicker.tick()),
        interval.toTicks(), interval.toTicks());

    Bukkit.getPluginManager().registerEvent(
        InventoryClickEvent.class, this, EventPriority.NORMAL,
        (listener, rawEvent) -> {
          InventoryClickEvent event = (InventoryClickEvent) rawEvent;
          if (content == null) return;
          HumanEntity whoClicked = event.getWhoClicked();
          if (!(whoClicked instanceof Player)) return;
          if (!viewers.contains(whoClicked)) return;
          int slot = event.getSlot();
          if (event.getCurrentItem() != null
              && slot >= 0 && slot < getDimensions().size()) {
            SkywarsPlayer player = SkywarsPlayer.getPlayer((Player) whoClicked);
            content.find(slot).ifPresent((item) -> item.click(player, event));
          }
        },
        Skywars.plugin());

    Bukkit.getPluginManager().registerEvent(
        InventoryCloseEvent.class, this, EventPriority.NORMAL,
        (listener, event) -> close((Player) ((InventoryCloseEvent) event).getPlayer()),
        Skywars.plugin());
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

}