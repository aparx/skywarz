package io.github.aparx.skywarz.game.item.items.idle;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.skywarz.game.item.StaticSkywarsItem;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.match.GameMatchState;
import io.github.aparx.skywarz.startup.Main;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-11 20:08
 * @since 1.0
 */
public class QuickstartItem extends StaticSkywarsItem {

  @ConfigMapping
  @Document("The item with which a player can interact")
  private WrappedItemStack item = ItemBuilder
      .builder(Material.NETHER_STAR)
      .lore(ChatColor.DARK_GRAY + "Click to quick start your lobby")
      .name(ChatColor.GREEN + "Quickstart")
      .wrap();

  public QuickstartItem() {
    super("quickstart", new GameMatchState[]{GameMatchState.IDLE});
    setSlot(7);
  }

  @Override
  protected ItemStack createItemStack(@NonNull GameMatch match, @NonNull Player initiator) {
    return item.getStack().clone();
  }

  @Override
  protected void handleClick(@NonNull GameMatch match, PlayerInteractEvent event) {
    event.setCancelled(true);
    SoundRecord.ACTION_SUCCESS.play(event.getPlayer());
    event.getPlayer().performCommand(String.format("%s start", Main.ROOT_COMMAND_NAME));
  }
}
