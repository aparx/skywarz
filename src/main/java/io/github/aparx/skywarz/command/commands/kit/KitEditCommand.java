package io.github.aparx.skywarz.command.commands.kit;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.kit.GameKit;
import io.github.aparx.skywarz.game.kit.GameKitManager;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import io.github.aparx.skywarz.startup.Main;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 17:55
 * @since 1.0
 */
public class KitEditCommand extends CommandNode {

  public KitEditCommand(@Nullable CommandNode parent) {
    super(CommandInfo.builder("edit")
        .permission(SkywarsPermission.SETUP)
        .args("<Kit...>")
        .description("Enters the kit edit mode for given kit")
        .build(), parent);
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    if (args.isEmpty())
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      Player entity = context.getPlayer();
      SkywarsPlayer player = SkywarsPlayer.getPlayer(entity);
      Preconditions.checkState(player.getPlayerData()
              .find(PlayerMatchData.class)
              .filter(PlayerMatchData::isInMatch)
              .isEmpty(),
          "Cannot modify kits while you are in a match!");
      String kitName = args.join();
      GameKitManager kitManager = Skywars.getInstance().getKitManager();
      Preconditions.checkState(kitManager.contains(kitName),
          String.format("Kit '%s' does not exist", kitName));
      Preconditions.checkState(player.getPlayerData()
              .getOrCreate(PlayerKitEditMode.class)
              .enter(kitManager.get(kitName)),
          "Could not enter kit edit mode");
      entity.sendMessage(Language.getInstance().substitute(List.of(
          "{successPrefix} You have entered the kit edit mode.",
          "{successPrefix} The kit will reflect your inventory when saved.",
          "{successPrefix} Use {0}'/{1} save'§a or {0}'/{1} cancel'§a to leave."
      ), ChatColor.GRAY, getParent().createCommand(Main.SHORT_COMMAND)));
      SoundRecord.ACTION_SUCCESS.play(player);
    }
  }

  // TODO abstract this and this command to an AbstractKitCommand
  @Override
  public List<String> onTabComplete(CommandContext context, CommandArgList args) {
    if (args.isEmpty()) return null;
    final String targetName = args.join().toLowerCase();
    return Skywars.getInstance().getKitManager().getKits().stream()
        .map(GameKit::getName)
        .filter((name) -> name.toLowerCase().startsWith(targetName))
        .collect(Collectors.toList());
  }
}
