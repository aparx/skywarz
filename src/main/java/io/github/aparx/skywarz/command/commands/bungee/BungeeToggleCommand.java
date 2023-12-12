package io.github.aparx.skywarz.command.commands.bungee;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.handler.MainConfig;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 16:13
 * @since 1.0
 */
public class BungeeToggleCommand extends CommandNode {
  public BungeeToggleCommand(@Nullable CommandNode parent) {
    super(CommandInfo.builder("toggle")
        .description("Toggle the bungeecord mode")
        .build(), parent);
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    if (!args.isEmpty())
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      CommandSender sender = context.getSender();
      MainConfig mainConfig = MainConfig.getInstance();
      String arenaName = mainConfig.getDedicatedArena();
      Preconditions.checkNotNull(arenaName, "Arena must be defined first (/sw bungee update)");
      mainConfig.setDedicated(!mainConfig.isDedicated());
      mainConfig.save();
      sender.sendMessage(Language.getInstance().substitute(
          "{successPrefix} {0} the bungeecord mode!",
          mainConfig.isDedicated() ? "Enabled" : "Disabled"));
    }
  }
}
