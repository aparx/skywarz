package io.github.aparx.skywarz.command.commands;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.language.LocalizableError;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.language.MessageKeys;
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
        Skywars.getInstance().getMatchManager().join(player, arena);
      }, (lang) -> lang.substitute(MessageKeys.Match.JOIN_ERROR));
    }
  }
}
