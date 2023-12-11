package io.github.aparx.skywarz.command.commands.arena.update;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.language.Language;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 06:27
 * @since 1.0
 */
public class ArenaSetTeamSize extends AbstractArenaCommand {

  private static final int ARENA_PARAMETER_INDEX = 0;
  private static final int NUMBER_PARAMETER_INDEX = 1;

  public ArenaSetTeamSize(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
            .name("size")
            .args("<Arena> <TeamSize>")
            .description("Update the maximum amount of players per team")
            .build(),
        ARENA_PARAMETER_INDEX,
        parent);
  }

  @Override
  protected void execute(SkywarsArena arena, CommandContext context, CommandArgList args) {
    if (args.length() != 1 + NUMBER_PARAMETER_INDEX)
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      int newTeamSize = args.get(NUMBER_PARAMETER_INDEX).getInt();
      Preconditions.checkArgument(newTeamSize >= 1, "Team size must at least be one");
      arena.getData().setSettings(arena.getData().getSettings().withTeamSize(newTeamSize));
      context.getSender().sendMessage(Language.getInstance().substitute(
          "{successPrefix} Updated size per team from {0} to {1}. (unsaved)",
          arena.getName(), newTeamSize));
    }
  }


}
