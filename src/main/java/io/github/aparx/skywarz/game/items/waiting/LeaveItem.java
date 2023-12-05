package io.github.aparx.skywarz.game.items.waiting;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.SkywarsCommand;
import io.github.aparx.skywarz.command.commands.LeaveCommand;
import io.github.aparx.skywarz.game.items.GameItem;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.startup.Main;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 06:57
 * @since 1.0
 */
public final class LeaveItem extends GameItem {

  public LeaveItem() {
    super("leave", MatchState.WAITING);
  }

  @Override
  protected ItemStack createItemStack(@NonNull Match match, @NonNull Player initiator) {
    return Skywars.getInstance().getConfigHandler().getItems().getLeave().getStack();
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
