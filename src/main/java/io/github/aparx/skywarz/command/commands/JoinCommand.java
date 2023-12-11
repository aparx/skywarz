package io.github.aparx.skywarz.command.commands;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.events.match.MatchCreateEvent;
import io.github.aparx.skywarz.events.match.MatchJoinEvent;
import io.github.aparx.skywarz.game.match.SkywarsMatchManager;
import io.github.aparx.skywarz.language.LocalizableError;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.game.match.SkywarsMatch;
import io.github.aparx.skywarz.language.MessageKeys;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 08:07
 * @since 1.0
 */
public class JoinCommand extends AbstractArenaCommand {

  private static final int ARENA_ARGUMENT_INDEX = 0;

  public JoinCommand() {
    super(CommandInfo.builder("join")
            .args("<Arena>")
            .description("Join the given arena")
            .build(),
        ARENA_ARGUMENT_INDEX);
  }

  @Override
  public void execute(SkywarsArena arena, CommandContext context, CommandArgList args) {
    if (args.length() != 1) context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      Player entity = context.getPlayer();
      SkywarsPlayer player = SkywarsPlayer.getPlayer(entity);
      PlayerMatchData data = player.getMatchData();
      if (data.isInMatch())
        throw new LocalizableError((lang) -> lang.substitute(MessageKeys.Errors.IN_A_MATCH));
      LocalizableError.localizeThrow(() -> {
        SkywarsMatchManager matchManager = Skywars.getInstance().getMatchManager();
        SkywarsMatch match = matchManager.find(arena).orElseGet(() -> {
          SkywarsMatch newMatch = SkywarsMatchManager.DEFAULT_MATCH_FACTORY.apply(arena);
          Preconditions.checkState(matchManager.register(newMatch), "Could not register match");
          MatchCreateEvent createEvent = new MatchCreateEvent(newMatch);
          Bukkit.getPluginManager().callEvent(createEvent);
          Preconditions.checkState(!createEvent.isCancelled(), "Match creation was cancelled");
          return newMatch;
        });
        MatchJoinEvent joinEvent = new MatchJoinEvent(match, entity);
        Bukkit.getPluginManager().callEvent(joinEvent);
        Preconditions.checkState(!joinEvent.isCancelled(), "Join was cancelled");
        Preconditions.checkState(match.getState().isJoinable(), "Match is not joinable");
        Preconditions.checkState(match.join(player));
      }, (lang) -> lang.substitute(MessageKeys.Match.JOIN_ERROR));
    }
  }
}
