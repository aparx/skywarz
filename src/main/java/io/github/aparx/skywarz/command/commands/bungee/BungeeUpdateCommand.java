package io.github.aparx.skywarz.command.commands.bungee;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.handler.MainConfig;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 16:13
 * @since 1.0
 */
public class BungeeUpdateCommand extends AbstractArenaCommand {
  public BungeeUpdateCommand(@Nullable CommandNode parent) {
    super(CommandInfo.builder("update")
        .description("Update the arena that is used for this server")
        .args("<Arena>")
        .build(), 0, parent);
  }

  @Override
  protected void execute(GameArena arena, CommandContext context, CommandArgList args) {
    if (args.length() != 1)
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      CommandSender sender = context.getSender();
      MainConfig mainConfig = MainConfig.getInstance();
      mainConfig.setDedicatedArena(arena.getName());
      mainConfig.save();
      sender.sendMessage(Language.getInstance().substitute(
          "{successPrefix} Updated the bungee auto join arena to '{0}'",
          arena.getName()));
      if (!mainConfig.isDedicated())
        sender.sendMessage(Language.getInstance().substitute(
            "{successPrefix} You still need to enable the bungeecord mode (/sw bungee toggle)"));
    }
  }

}
