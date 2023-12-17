package io.github.aparx.skywarz.command.commands.kit;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.kit.GameKit;
import io.github.aparx.skywarz.game.kit.GameKitManager;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 17:55
 * @since 1.0
 */
public class KitDeleteCommand extends CommandNode {

  public KitDeleteCommand(@Nullable CommandNode parent) {
    super(CommandInfo.builder("delete")
        .permission(SkywarsPermission.SETUP)
        .args("<Kit...>")
        .description("Deletes a specific kit")
        .build(), parent);
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    if (args.isEmpty())
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      String kitName = args.join();
      GameKitManager kitManager = Skywars.getInstance().getKitManager();
      Preconditions.checkState(kitManager.contains(kitName), "Kit does not exist");
      GameKit gameKit = kitManager.get(kitName);
      Preconditions.checkState(kitManager.getKits().remove(gameKit), "Could not delete kit");
      kitManager.save();
      context.getSender().sendMessage(Language.getInstance().substitute(
          "{successPrefix} Kit '{0}' was deleted successfully",
          gameKit.getName()));
    }
  }

  // TODO abstract this and this command to an AbstractKitCommand
  @Override
  public List<String> onTabComplete(CommandContext context, CommandArgList args) {
    if (args.isEmpty()) return null;
    final String targetName = args.join();
    return Skywars.getInstance().getKitManager().getKits().stream()
        .map(GameKit::getName)
        .filter((name) -> StringUtils.startsWithIgnoreCase(name, targetName))
        .collect(Collectors.toList());
  }
}
