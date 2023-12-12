package io.github.aparx.skywarz.game.match.listener;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.match.GameMatchState;
import io.github.aparx.skywarz.game.team.GameTeam;
import io.github.aparx.skywarz.handler.MainConfig;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.LazyVariableLookup;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.language.VariablePopulator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 02:54
 * @since 1.0
 */
public class SkywarsMatchListener implements Listener {

  private final ImmutableSet<@NonNull String> everyoneChatTags =
      ImmutableSet.copyOf(MainConfig.getInstance().getEveryoneChatTargetTags());

  @EventHandler
  void onQuit(PlayerQuitEvent event) {
    SkywarsPlayer.findPlayer(event.getPlayer()).ifPresent((player) -> {
      PlayerMatchData matchData = player.getMatchData();
      GameMatch match = matchData.getMatch();
      if (matchData.isInMatch() && match != null)
        match.leave(player);
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onGeneralChat(AsyncPlayerChatEvent event) {
    Player entity = event.getPlayer();
    if (event.isCancelled()) return;
    SkywarsPlayer.findPlayer(entity).ifPresent((player) -> {
      PlayerMatchData matchData = player.getMatchData();
      GameMatch match = matchData.getMatch();
      if (matchData.isInMatch() && match != null) {
        event.setCancelled(true);
        if (!match.isState(GameMatchState.PLAYING)) {
          match.getAudience().sendMessage("%s: %s", entity.getDisplayName(), event.getMessage());
        } else if (matchData.isSpectator()) {
          LazyVariableLookup lookup = new LazyVariableLookup();
          VariablePopulator.addPlayer(lookup, event.getPlayer(), ArrayPath.of("sender"));
          lookup.set(ArrayPath.of("message"), event.getMessage());
          String message = Language.getInstance().substitute(MessageKeys.Match.CHAT_SPECTATOR,
              lookup);
          match.getAudience().dead().forEach((dead) -> dead.sendMessage(message));
        } else if (matchData.isInTeam()) {
          GameTeam team = matchData.getTeam();
          Preconditions.checkNotNull(team);
          CommandArgList argList = CommandArgList.parse(event.getMessage());
          if (argList.isEmpty()) return;
          String arg = argList.getString(0);
          boolean isEveryone = team.size() <= 1 || (!arg.isEmpty()
              && everyoneChatTags.contains(arg.toLowerCase()));
          LazyVariableLookup lookup = new LazyVariableLookup();
          VariablePopulator.addPlayer(lookup, event.getPlayer(), ArrayPath.of("sender"));
          lookup.set(ArrayPath.of("message"), isEveryone
              ? argList.join(Math.min(1, argList.length() - 1))
              : event.getMessage());
          (isEveryone ? match.getAudience() : team).sendFormattedMessage(
              isEveryone ? MessageKeys.Match.CHAT_ALL : MessageKeys.Match.CHAT_TEAM, lookup);
        }
      }
    });
  }

}
