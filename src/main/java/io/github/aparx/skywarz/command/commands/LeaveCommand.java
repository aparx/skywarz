package io.github.aparx.skywarz.command.commands;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.exceptions.CommandError;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.handler.configs.Language;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 08:07
 * @since 1.0
 */
public class LeaveCommand extends CommandNode {

  public LeaveCommand() {
    super(CommandInfo.builder("leave")
        .permission("skywarz.play")
        .description("Leave the arena you are in")
        .build());
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    SkywarsPlayer player = SkywarsPlayer.getPlayer(context.getPlayer());
    PlayerMatchData data = player.getMatchData();
    if (!data.isInMatch())
      throw new CommandError((e, l) -> l.substitute(l.getErrorNotInMatch()));
    Match match = data.getMatch();
    Preconditions.checkNotNull(match, "Already left the match");
    Preconditions.checkState(match.leave(player), "Cannot leave match");
    player.sendMessage(Language::getSuccessLeaveMatch);
  }
}
