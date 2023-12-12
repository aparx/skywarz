package io.github.aparx.skywarz.command.commands.stats;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.StatsCommand;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-11 12:04
 * @since 1.0
 */
public class StatsResetCommand extends CommandNode {


  public StatsResetCommand(@NonNull StatsCommand parent) {
    super(CommandInfo.builder("reset")
        .description("Reset the statistics for a specific player")
        .args("<player>")
        .permission(SkywarsPermission.STATS_MANIPULATE)
        .build(), parent);
    Preconditions.checkNotNull(parent, "Parent must not be null");
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    Preconditions.checkState(Skywars.getInstance().getDatabase().isEnabled(),
        "Not possible: the database is not active");
    if (args.isEmpty()) {
      // perform parent command with parent arguments
      getParent().execute(context, context.getInitialArgs().subargs(1, 1 + getIndex()));
    } else if (args.length() != 1) {
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    } else {
      OfflinePlayer offlinePlayer = args.get(0).getOfflinePlayer();
      context.getSender().sendMessage(Language.getInstance().substitute(
          "{prefix} §7Deleting statistics for player {0} (...)",
          offlinePlayer.getName()));
      Skywars.getInstance().getDatabase().getStatsManager()
          .delete(offlinePlayer.getUniqueId())
          .thenAccept((i) -> {
            context.getSender().sendMessage(Language.getInstance().substitute(
                "{successPrefix} Deleted {0} stat entries from the database", i));
          })
          .exceptionally((t) -> {
            context.getSender().sendMessage(Language.getInstance().substitute(
                "{warningPrefix} Could not delete stat entries from player: {0}", t.getMessage()));
            return null;
          });
    }
  }
}
