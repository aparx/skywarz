package io.github.aparx.skywarz.command.commands.bungee;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.handler.MainConfig;
import io.github.aparx.skywarz.language.Language;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 16:13
 * @since 1.0
 */
public class BungeeToggleCommand extends CommandNode {
  public BungeeToggleCommand(@Nullable CommandNode parent) {
    super(CommandInfo.builder("toggle")
        .description("Toggle the Bungeecord mode")
        .args("(true:false)")
        .build(), parent);
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    if (args.length() > 1)
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      CommandSender sender = context.getSender();
      MainConfig mainConfig = MainConfig.getInstance();
      String name = Objects.toString(mainConfig.getBungeeArena());
      boolean newState = !args.isEmpty()
          ? "true".equalsIgnoreCase(args.first().get())
          : !mainConfig.isBungeeEnabled();
      if (newState) Preconditions.checkArgument(
          Skywars.getInstance().getArenaManager().find(name).isPresent(),
          String.format("Cannot enable Bungeecord: Arena '%s' does not exist", name));
      mainConfig.setBungeeEnabled(newState);
      mainConfig.save();
      sender.sendMessage(Language.getInstance().substitute(
          "{successPrefix} {0} Bungeecord!",
          mainConfig.isBungeeEnabled() ? "Enabled" : "Disabled"));
    }
  }

  @Override
  public List<String> onTabComplete(CommandContext context, CommandArgList args) {
    return args.length() == 1 ? List.of("true", "false") : null;
  }
}
