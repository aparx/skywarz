package io.github.aparx.skywarz.game.item.items;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.skywarz.command.SkywarsCommand;
import io.github.aparx.skywarz.command.commands.LeaveCommand;
import io.github.aparx.skywarz.game.item.SkywarsItem;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.match.GameMatchState;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import io.github.aparx.skywarz.startup.Main;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
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
 * @version 2023-12-04 06:57
 * @since 1.0
 */
public final class LeaveItem extends SkywarsItem {

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
    super("leave", new GameMatchState[]{GameMatchState.IDLE, GameMatchState.PLAYING, GameMatchState.DONE});
  }

  @Override
  public void save() {
    setHeaderIfAbsent(SkywarsConfigHandler.createHeader(
        "Leave item configuration",
        "Edit to update the item."
    ));
    super.save();
  }

  @Override
  protected ItemStack createItemStack(@NonNull GameMatch match, @NonNull Player initiator) {
    return item.getStack().clone();
  }

  @Override
  protected void handleClick(@NonNull GameMatch match, PlayerInteractEvent event) {
    Player player = event.getPlayer();
    SkywarsCommand.forest.getTrees().stream()
        .filter((node) -> node instanceof LeaveCommand)
        .map((node) -> node.createCommand(Main.FULL_COMMAND))
        .findFirst()
        .ifPresent(player::performCommand);
    SoundRecord.ACTION_SUCCESS.play(player);
  }

}
