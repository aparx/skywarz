package io.github.aparx.skywarz.command.commands;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.command.exceptions.CommandError;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.language.MessageKeys;

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
            .permission("skywarz.play")
            .description("Join the given arena")
            .build(),
        ARENA_ARGUMENT_INDEX);
  }

  @Override
  public void execute(Arena arena, CommandContext context, CommandArgList args) {
    if (args.length() != 1) context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      SkywarsPlayer player = SkywarsPlayer.getPlayer(context.getPlayer());
      PlayerMatchData data = player.getMatchData();
      if (data.isInMatch())
        throw new CommandError((lang) -> lang.substitute(MessageKeys.Errors.IN_A_MATCH));
      Match match = Skywars.getInstance().getMatchManager().getOrCreate(arena);
      if (!match.getState().isJoinable())
        throw new CommandError((lang) -> lang.substitute(MessageKeys.Match.JOIN_ERROR));
      Preconditions.checkState(match.join(player), "Cannot join match");
      player.sendFormattedMessage(MessageKeys.Match.JOIN_SUCCESS);
    }
  }
}
