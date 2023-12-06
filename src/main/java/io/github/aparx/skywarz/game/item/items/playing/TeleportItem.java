package io.github.aparx.skywarz.game.item.items.playing;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.skywarz.game.inventory.InventoryDimensions;
import io.github.aparx.skywarz.game.inventory.InventoryItem;
import io.github.aparx.skywarz.game.inventory.content.PaginatingInventory;
import io.github.aparx.skywarz.game.item.StaticGameItem;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.SkullItem;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import org.bukkit.Bukkit;
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
public final class TeleportItem extends StaticGameItem {

  @ConfigMapping("item.item")
  private WrappedItemStack item = ItemBuilder
      .builder(Material.COMPASS)
      .lore("§7Click to teleport to players")
      .name("§7Teleporter")
      .enchants(Map.of(Enchantment.LUCK, 2))
      .flags(ItemFlag.HIDE_ENCHANTS)
      .wrap();

  public TeleportItem() {
    super("teleport", new MatchState[]{MatchState.PLAYING});
    setSlot(0);
  }

  @Override
  protected ItemStack createItemStack(@NonNull Match match, @NonNull Player initiator) {
    return item.getStack();
  }

  @Override
  protected void handleClick(@NonNull Match match, PlayerInteractEvent event) {
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
