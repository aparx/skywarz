package io.github.aparx.skywarz.game.item.items.idle;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.game.item.StaticSkywarsItem;
import io.github.aparx.skywarz.game.item.items.idle.kit.KitInventory;
import io.github.aparx.skywarz.game.match.SkywarsMatch;
import io.github.aparx.skywarz.game.match.SkywarsMatchState;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 23:00
 * @since 1.0
 */
@Document("Kit Selector")
public class KitSelectorItem extends StaticSkywarsItem {

  @ConfigMapping
  @Document("The item with which a player can interact")
  private WrappedItemStack item = ItemBuilder
      .builder(Material.CHEST)
      .lore(ChatColor.DARK_GRAY + "Click to select your kit")
      .name(ChatColor.AQUA + "Kit selector")
      .enchants(Map.of(Enchantment.LUCK, 1))
      .flags(ItemFlag.HIDE_ENCHANTS)
      .wrap();

  @ConfigMapping("menu.title")
  @Document("The title of the selector inventory")
  private String menuTitle = "Kit Selector";

  public KitSelectorItem() {
    super("kit selector", new SkywarsMatchState[]{SkywarsMatchState.IDLE});
    setSlot(0);
  }

  @Override
  protected ItemStack createItemStack(@NonNull SkywarsMatch match, @NonNull Player initiator) {
    return item.getStack().clone();
  }

  @Override
  protected void handleClick(@NonNull SkywarsMatch match, PlayerInteractEvent event) {
    event.setCancelled(true);
    SkywarsPlayer player = SkywarsPlayer.getPlayer(event.getPlayer());
    KitInventory kitInventory = new KitInventory(match, player, menuTitle);
    kitInventory.fillInventory();
    kitInventory.open(player.getOnline());
    SoundRecord.OPEN_INVENTORY.play(player);
  }

}
