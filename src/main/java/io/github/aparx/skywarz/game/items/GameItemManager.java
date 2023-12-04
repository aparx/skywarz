package io.github.aparx.skywarz.game.items;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.items.waiting.LeaveItem;
import io.github.aparx.skywarz.game.items.waiting.TeamSelectorItem;
import io.github.aparx.skywarz.handler.DefaultSkywarsHandler;
import io.github.aparx.skywarz.utils.collection.KeyedByClassSet;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 07:05
 * @since 1.0
 */
@Getter
public final class GameItemManager extends DefaultSkywarsHandler implements Listener {

  private final KeyedByClassSet<GameItem> items = new KeyedByClassSet<>() {

    @Override
    public boolean add(@NonNull GameItem gameItem) {
      synchronized (handlerLock) {
        if (!super.add(gameItem)) return false;
        if (isLoaded()) gameItem.register();
        return true;
      }
    }

    @Override
    @CanIgnoreReturnValue
    public boolean remove(Object value) {
      synchronized (handlerLock) {
        if (!super.remove(value)) return false;
        if (isLoaded()) ((GameItem) value).unregister();
        return true;
      }
    }
  };

  public GameItemManager() {
    add(new LeaveItem());
    add(new TeamSelectorItem());
  }

  @CanIgnoreReturnValue
  public boolean add(GameItem gameItem) {
    Preconditions.checkNotNull(gameItem, "Item must not be null");
    return items.add(gameItem);
  }

  @CanIgnoreReturnValue
  public boolean remove(GameItem gameItem) {
    return items.remove(gameItem);
  }

  @Override
  protected void onLoad() {
    items.forEach(GameItem::register);
    Bukkit.getPluginManager().registerEvents(this, Skywars.plugin());
  }

  @Override
  protected void onUnload() {
    items.forEach(GameItem::unregister);
    HandlerList.unregisterAll(this);
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onClick(PlayerInteractEvent event) {
    if (event.getItem() != null)
      items.stream()
          .filter((item) -> item.isItem(event.getItem()))
          .findFirst()
          .ifPresent((item) -> item.filterMatch(event.getPlayer())
              .ifPresent((match) -> item.handleClick(match, event)));
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onDrop(PlayerDropItemEvent event) {
    if (!event.isCancelled()) items.stream()
        .filter((item) -> item.isItem(event.getItemDrop().getItemStack()))
        .findFirst()
        .ifPresent((item) -> item.filterMatch(event.getPlayer())
            .ifPresent((match) -> item.handleDrop(match, event)));
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onInventory(InventoryClickEvent event) {
    HumanEntity whoClicked = event.getWhoClicked();
    if (!(whoClicked instanceof Player)) return;
    if (!event.isCancelled()) items.stream()
        .filter((item) -> item.isItem(event.getCurrentItem()))
        .findFirst()
        .ifPresent((item) -> item.filterMatch((Player) whoClicked)
            .ifPresent((match) -> item.handleInventory(match, event)));
  }

}
