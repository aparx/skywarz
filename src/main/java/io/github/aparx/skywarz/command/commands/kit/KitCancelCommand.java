package io.github.aparx.skywarz.command.commands.kit;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 17:55
 * @since 1.0
 */
public class KitCancelCommand extends CommandNode {

  public KitCancelCommand(@Nullable CommandNode parent) {
    super(CommandInfo.builder("cancel")
        .permission(SkywarsPermission.SETUP)
        .description("Leave the kit edit mode without saving anything")
        .build(), parent);
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    if (!args.isEmpty())
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      Player entity = context.getPlayer();
      SkywarsPlayer.findPlayer(entity)
          .map(SkywarsPlayer::getPlayerData)
          .flatMap((data) -> data.find(PlayerKitEditMode.class))
          .filter(PlayerKitEditMode::isInMode)
          .orElseThrow(() -> new IllegalStateException("Not in edit mode"))
          .leave();
      SoundRecord.ACTION_SUCCESS.play(entity);
      entity.sendMessage(Language.getInstance().substitute(
          "{successPrefix} Left the kit edit mode (left unsaved)!"));
    }
  }
}
