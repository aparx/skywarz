package io.github.aparx.skywarz.command.commands.arena;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
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
  public void execute(SkywarsArena arena, CommandContext context, CommandArgList args) {
    arena.save();
    Language language = Language.getInstance();
    StringBuilder builder = new StringBuilder("{successPrefix} Saved arena '{0}'");
    if (arena.isCompleted())
      builder.append(" and completed the setup");
    builder.append('!');
    context.getSender().sendMessage(language.substitute(builder.toString(), arena.getName()));
  }
}
