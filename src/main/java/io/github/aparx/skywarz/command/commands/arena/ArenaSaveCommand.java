package io.github.aparx.skywarz.command.commands.arena;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.arena.ArenaBox;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
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
  public void execute(GameArena arena, CommandContext context, CommandArgList args) {
    ArenaBox box = arena.getData().getBox();
    if (box.isCompleted())
      arena.getData().getSpawns().forEach((teamEnum, group) -> group.stream()
          .filter((e) -> !arena.getData().getBox().isWithin(e.getValue()))
          .findFirst()
          .ifPresent((entry) -> {
            throw new IllegalArgumentException(String.format(
                "Cannot save: team spawn %s from %s is outside the playground. "
                    + "To proceed either remove that spawn or include it within the playground.",
                entry.getKey(), teamEnum.getDefaultName()));
          }));
    arena.save();
    Language language = Language.getInstance();
    StringBuilder builder = new StringBuilder("{successPrefix} Saved arena '{0}'");
    if (arena.isCompleted())
      builder.append(" and completed the setup");
    builder.append('!');
    context.getSender().sendMessage(language.substitute(builder.toString(), arena.getName()));
  }
}
