package io.github.aparx.skywarz.game.item.items.playing;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.skywarz.game.inventory.InventoryDimensions;
import io.github.aparx.skywarz.game.inventory.InventoryItem;
import io.github.aparx.skywarz.game.inventory.content.PaginatingInventory;
import io.github.aparx.skywarz.game.item.StaticSkywarsItem;
import io.github.aparx.skywarz.game.match.SkywarsMatch;
import io.github.aparx.skywarz.game.match.SkywarsMatchState;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.SkullItem;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 06:57
 * @since 1.0
 */
@Document("Teleporter used from spectators")
public final class TeleportItem extends StaticSkywarsItem {

  @ConfigMapping
  @Document("The item with which a player can interact")
  private WrappedItemStack item = ItemBuilder
      .builder(Material.COMPASS)
      .lore(ChatColor.DARK_GRAY + "Click to teleport to players")
      .name(ChatColor.GRAY + "Teleporter")
      .enchants(Map.of(Enchantment.LUCK, 2))
      .flags(ItemFlag.HIDE_ENCHANTS)
      .wrap();

  public TeleportItem() {
    super("teleporter", new SkywarsMatchState[]{SkywarsMatchState.PLAYING});
    setSlot(0);
  }

  @Override
  protected ItemStack createItemStack(@NonNull SkywarsMatch match, @NonNull Player initiator) {
    return item.getStack();
  }

  @Override
  protected void handleClick(@NonNull SkywarsMatch match, PlayerInteractEvent event) {
    ArrayList<InventoryItem> items = new ArrayList<>();
    Player player = event.getPlayer();
    PaginatingInventory inventory = new PaginatingInventory(null, TickDuration.ofSecond(),
        InventoryDimensions.ofRows(2), items, "Teleporter") {
      @Override
      public void updateInventory(long ticks) {
        items.clear();
        match.getAudience().alive().forEach((alive) -> {
          items.add(InventoryItem.of(SkullItem.of(ItemBuilder.builder()
                      .material(Material.PLAYER_HEAD)
                      .name(alive.getDisplayName())
                      .build(),
                  Bukkit.getOfflinePlayer(alive.getId())),
              (p, e) -> {
                alive.findOnline()
                    .filter((x) -> alive.getMatchData().isInMatch()
                        && !alive.getMatchData().isSpectator())
                    .ifPresent(player::teleport);
                e.setCancelled(true);
              }));
        });
        super.updateInventory(ticks);
      }
    };
    player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0F, 1F);
    inventory.open(player);
  }

}
