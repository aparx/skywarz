package io.github.aparx.skywarz.command.commands.arena.set;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.handler.configs.Language;
import io.github.aparx.skywarz.skywars.arena.Arena;
import io.github.aparx.skywarz.skywars.arena.MutableArenaBox;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 01:19
 * @since 1.0
 */
public class ArenaSetPointCommand extends AbstractArenaCommand {

  private static final int ARENA_ARGUMENT_INDEX = 1;

  public ArenaSetPointCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
            .name("point")
            .args("<1:2> <Arena>")
            .description("Set a bounding point of the arena's play area")
            .build(),
        parent, ARENA_ARGUMENT_INDEX);
  }

  @Override
  public void execute(Arena arena, CommandContext context, CommandArgList args) {
    Player player = context.getPlayer();
    final int maxPoint = MutableArenaBox.Point.CONSTANTS.size();
    int numericalPoint = args.get(0).getInt();
    Preconditions.checkState(numericalPoint >= 1 && numericalPoint <= maxPoint,
        String.format("Unrecognized point. Must be between %s and %s!", 1, maxPoint));
    MutableArenaBox.Point point = MutableArenaBox.Point.ofIndex(numericalPoint - 1);
    arena.getBox().setPoint(point, player.getLocation().toVector());
    arena.setWorld(player.getWorld());
    player.sendMessage(Language.getLanguage().substitute(
        "{successPrefix} Updated point {0} ({1}) of {2}!",
        point.name(), numericalPoint, arena.getName()));
  }

  @Override
  public List<String> onTabComplete(CommandContext context, CommandArgList args) {
    List<String> suggestions = super.onTabComplete(context, args);
    if ((suggestions == null || suggestions.isEmpty()) && args.length() == 1)
      suggestions = MutableArenaBox.Point.CONSTANTS.stream()
          .map(point -> String.valueOf(1 + point.ordinal()))
          .collect(Collectors.toList());
    return suggestions;
  }
}
