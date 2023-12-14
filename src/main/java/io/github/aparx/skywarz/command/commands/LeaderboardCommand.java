package io.github.aparx.skywarz.command.commands;

import com.google.common.base.Suppliers;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.database.leaderboard.Leaderboard;
import io.github.aparx.skywarz.database.object.CachableLazyObject;
import io.github.aparx.skywarz.database.object.FetchableObjectState;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsAccumulator;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.LazyVariableLookup;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.language.VariablePopulator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-14 17:01
 * @since 1.0
 */
public class LeaderboardCommand extends CommandNode {

  public LeaderboardCommand() {
    super(CommandInfo.builder("leaderboard")
        .aliases("lb", "top")
        .build());
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    CachableLazyObject<List<PlayerStatsAccumulator>> leaderboardContent =
        Leaderboard.getMainLeaderboard().getContent();
    CommandSender sender = context.getSender();
    Language language = Language.getInstance();
    if (leaderboardContent.getState() != FetchableObjectState.FRESH)
      sender.sendMessage(language.substitute(MessageKeys.Stats.FETCHING));
    leaderboardContent.fetch().thenAccept((stats) -> {
      LazyVariableLookup outerLookup = new LazyVariableLookup();
      outerLookup.set("content", Suppliers.memoize(() -> {
        StringBuilder builder = new StringBuilder();
        ArrayPath prefix = ArrayPath.of("player");
        Iterator<PlayerStatsAccumulator> iterator = stats.iterator();
        for (int i = 0; iterator.hasNext(); ++i) {
          PlayerStatsAccumulator next = iterator.next();
          LazyVariableLookup innerLookup = new LazyVariableLookup();
          VariablePopulator.addPlayer(innerLookup, Bukkit.getOfflinePlayer(next.getId()), prefix);
          VariablePopulator.addStats(innerLookup, next, prefix.add("stats"));
          innerLookup.set(ArrayPath.of("place"), 1 + i);
          builder.append(language.substitute(MessageKeys.Stats.LEADERBOARD_LINE, innerLookup)).append('\n');
        }
        return builder.toString();
      }));
      sender.sendMessage(language.substitute(MessageKeys.Stats.LEADERBOARD_OVERVIEW, outerLookup));
    });
  }
}
