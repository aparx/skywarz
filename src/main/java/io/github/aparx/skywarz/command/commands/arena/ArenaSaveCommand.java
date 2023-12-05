package io.github.aparx.skywarz.command.commands.arena;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.language.Language;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 01:19
 * @since 1.0
 */
public class ArenaSaveCommand extends AbstractArenaCommand {

  private static final int ARENA_ARGUMENT_INDEX = 0;

  public ArenaSaveCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
            .name("save")
            .args("<Arena>")
            .description("Saves changes made to the target arena")
            .build(),
        ARENA_ARGUMENT_INDEX,
        parent);
  }

  @Override
  public void execute(Arena arena, CommandContext context, CommandArgList args) {
    Skywars.getInstance().getArenaManager().saveArena(arena);
    Language language = Language.getInstance();
    context.getSender().sendMessage(language.substitute(
        "{successPrefix} Saved arena {0}!", arena.getName()));
    if (arena.isCompleted())
      context.getSender().sendMessage(language.substitute(
          "{prefix} The setup has been completed for this arena."));
  }
}
