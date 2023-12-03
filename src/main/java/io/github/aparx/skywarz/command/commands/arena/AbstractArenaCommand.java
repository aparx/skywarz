package io.github.aparx.skywarz.command.commands.arena;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.skywars.arena.Arena;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 01:35
 * @since 1.0
 */
public abstract class AbstractArenaCommand extends CommandNode {

  private final @NonNegative int arenaArgumentIndex;

  public AbstractArenaCommand(
      @NonNull CommandInfo info,
      @NonNull CommandNode parent,
      @NonNegative int arenaArgumentIndex) {
    super(info, Preconditions.checkNotNull(parent));
    this.arenaArgumentIndex = arenaArgumentIndex;
  }

  protected abstract void execute(Arena arena, CommandContext context, CommandArgList args);

  @Override
  public final void execute(CommandContext context, CommandArgList args) {
    if (args.length() <= arenaArgumentIndex)
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      String name = args.get(arenaArgumentIndex).get();
      Arena arena = Skywars.getInstance().getArenaManager().find(name)
          .orElseThrow(() -> new IllegalArgumentException(
              String.format("Cannot find arena %s", name)));
      execute(arena, context, args);
    }
  }

  @Override
  public List<String> onTabComplete(CommandContext context, CommandArgList args) {
    if (args.isEmpty()) return null;
    if (args.length() <= arenaArgumentIndex) return null;
    String join = args.join(arenaArgumentIndex);
    return Skywars.getInstance().getArenaManager().stream()
        .map(Arena::getName)
        .filter(name -> join.isEmpty() || name.startsWith(join))
        .collect(Collectors.toList());
  }

}
