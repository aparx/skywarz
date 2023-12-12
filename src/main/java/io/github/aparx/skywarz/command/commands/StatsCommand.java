package io.github.aparx.skywarz.command.commands;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.stats.StatsResetCommand;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.database.object.CachableLazyObject;
import io.github.aparx.skywarz.database.object.CachableLazyObjectRegister;
import io.github.aparx.skywarz.database.object.FetchableObjectState;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsAccumulator;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.LazyVariableLookup;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.language.VariablePopulator;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-10 08:06
 * @since 1.0
 */
public class StatsCommand extends CommandNode {

  private static final int OTHER_ARGUMENT_INDEX = 0;

  public StatsCommand() {
    super(CommandInfo.builder("stats")
        .description("Show statistics of a player")
        .permission(SkywarsPermission.merge(SkywarsPermission.STATS_SELF,
            SkywarsPermission.STATS_OTHER))
        .args("(<player>)")
        .build());
    add(new StatsResetCommand(this));
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {

    if (args.length() > 1 + OTHER_ARGUMENT_INDEX)
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      Preconditions.checkState(Skywars.getInstance().getDatabase().isEnabled(),
          "Not possible: the database is not active");
      Player player = context.getPlayer();
      getTarget(player, context, args).ifPresent((target) -> {
        CachableLazyObjectRegister<UUID, PlayerStatsAccumulator> registry =
            Skywars.getInstance().getDatabase().getStatsManager().getRegistry();
        CachableLazyObject<PlayerStatsAccumulator> fetcher =
            registry.getOrCreate(target.getUniqueId());
        CompletableFuture<? extends PlayerStatsAccumulator> future = fetcher.fetch();
        Language language = Language.getInstance();
        LazyVariableLookup lookup = new LazyVariableLookup();
        VariablePopulator.addPlayer(lookup, target, ArrayPath.of("target"), "-");
        if (fetcher.getState() != FetchableObjectState.FRESH)
          player.sendMessage(language.get(MessageKeys.Stats.FETCHING).substitute(lookup));
        future.thenAccept((stats) -> {
          if (stats.isEmpty()) {
            player.sendMessage(language.get(player.equals(target)
                    ? MessageKeys.Stats.NONE_SELF
                    : MessageKeys.Stats.NONE_OTHER)
                .substitute(lookup));
            registry.remove(target.getUniqueId());
          } else {
            VariablePopulator.addStats(lookup, stats, ArrayPath.of("target", "total"));
            player.sendMessage(language.get(MessageKeys.Stats.OVERVIEW).substitute(lookup));
          }
        });
      });
    }
  }

  public static Optional<OfflinePlayer> getTarget(
      Player executor, CommandContext context, CommandArgList args) {
    boolean hasTargetDefined = args.length() == 1 + OTHER_ARGUMENT_INDEX;
    if (!hasTargetDefined && SkywarsPermission.STATS_SELF.has(executor))
      return Optional.of(executor);
    else if (hasTargetDefined && SkywarsPermission.STATS_OTHER.has(executor))
      return Optional.of(args.get(OTHER_ARGUMENT_INDEX).getOfflinePlayer());
    context.setStatus(CommandContext.Status.ERROR_PERMISSION);
    return Optional.empty();
  }
}
