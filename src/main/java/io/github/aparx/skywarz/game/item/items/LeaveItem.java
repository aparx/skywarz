package io.github.aparx.skywarz.game.item.items;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.skywarz.command.SkywarsCommand;
import io.github.aparx.skywarz.command.commands.LeaveCommand;
import io.github.aparx.skywarz.game.item.GameItem;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.startup.Main;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 06:57
 * @since 1.0
 */
public final class LeaveItem extends GameItem {

  public static final int SLOT = 8;

  @ConfigMapping("item.item")
  private WrappedItemStack item = ItemBuilder
      .builder(Material.RED_DYE)
      .lore("§8Click to leave this match")
      .name("§cLeave")
      .enchants(Map.of(Enchantment.LUCK, 2))
      .flags(ItemFlag.HIDE_ENCHANTS)
      .wrap();

  public LeaveItem() {
    super("leave", new MatchState[]{MatchState.IDLE, MatchState.PLAYING, MatchState.DONE});
  }

  @Override
  protected ItemStack createItemStack(@NonNull Match match, @NonNull Player initiator) {
    return item.getStack().clone();
  }

  @Override
  protected void handleClick(@NonNull Match match, PlayerInteractEvent event) {
    Player player = event.getPlayer();
    SkywarsCommand.tree.getRoots().stream()
        .filter((node) -> node instanceof LeaveCommand)
        .map((node) -> node.createCommand(Main.COMMAND_NAME))
        .findFirst()
        .ifPresent(player::performCommand);
    player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0F, 1F);
  }

}
