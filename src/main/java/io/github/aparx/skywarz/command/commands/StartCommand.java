package io.github.aparx.skywarz.command.commands;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.language.LocalizableError;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.permission.Permission;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 08:07
 * @since 1.0
 */
public class StartCommand extends CommandNode {

  public static final TickDuration QUICKSTART_TARGET = TickDuration.of(TimeUnit.SECONDS, 5);

  public StartCommand() {
    super(CommandInfo.builder("start")
        .permission(Permission.QUICKSTART)
        .description("Quickly start the game you joined")
        .build());
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    SkywarsPlayer player = SkywarsPlayer.getPlayer(context.getPlayer());
    PlayerMatchData data = player.getMatchData();
    if (!data.isInMatch())
      throw new LocalizableError((lang) -> lang.substitute(MessageKeys.Errors.NOT_IN_A_MATCH));
    try {
      Match match = data.getMatch();
      Preconditions.checkNotNull(match);
      Preconditions.checkState(match.isState(MatchState.WAITING));
      GamePhase phase = match.getCycler().getPhase().orElseThrow();
      TickDuration passed = phase.getDuration().add(QUICKSTART_TARGET.multiply(-1));
      Preconditions.checkState(!phase.getTicker().hasElapsed(QUICKSTART_TARGET));
      phase.getTicker().set(passed);
      player.sendFormattedMessage(MessageKeys.Match.QUICKSTART_SUCCESS);
    } catch (Exception e) {
      throw new LocalizableError((lang) -> lang.substitute(MessageKeys.Match.QUICKSTART_ERROR));
    }
  }
}
