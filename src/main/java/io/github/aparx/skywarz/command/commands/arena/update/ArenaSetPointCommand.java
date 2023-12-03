package io.github.aparx.skywarz.command.commands.arena.update;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.game.arena.ArenaData;
import io.github.aparx.skywarz.handler.configs.Language;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.game.arena.ArenaBox;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
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
        ARENA_ARGUMENT_INDEX,
        parent);
  }

  @Override
  public void execute(Arena arena, CommandContext context, CommandArgList args) {
    ArenaData data = arena.getData();
    Player player = context.getPlayer();
    final int maxPoint = ArenaBox.Point.values().length;
    int numericalPoint = args.get(0).getInt();
    Preconditions.checkState(numericalPoint >= 1 && numericalPoint <= maxPoint,
        String.format("Unrecognized point. Must be between %s and %s!", 1, maxPoint));
    ArenaBox.Point point = ArenaBox.Point.ofIndex(numericalPoint - 1);
    data.getBox().setPoint(point, player.getLocation().toVector());
    data.setWorld(player.getWorld());
    player.sendMessage(Language.getLanguage().substitute(
        "{successPrefix} Updated point {0} ({1}) of {2}!",
        point.name(), numericalPoint, arena.getName()));
  }

  @Override
  public List<String> onTabComplete(CommandContext context, CommandArgList args) {
    List<String> suggestions = super.onTabComplete(context, args);
    if ((suggestions == null || suggestions.isEmpty()) && args.length() == 1)
      suggestions = Arrays.stream(ArenaBox.Point.values())
          .map(point -> String.valueOf(1 + point.ordinal()))
          .collect(Collectors.toList());
    return suggestions;
  }
}
