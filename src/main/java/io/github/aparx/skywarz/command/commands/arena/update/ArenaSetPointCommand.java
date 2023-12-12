package io.github.aparx.skywarz.command.commands.arena.update;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.arena.ArenaData;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.game.arena.ArenaBox;
import io.github.aparx.skywarz.language.Language;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 01:19
 * @since 1.0
 */
public class ArenaSetPointCommand extends AbstractArenaCommand {

  private static final int ARENA_ARGUMENT_INDEX = 0;

  public ArenaSetPointCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
            .name("point")
            .args("<Arena> <1:2>")
            .description("Set a bounding point of the arena's playground")
            .build(),
        ARENA_ARGUMENT_INDEX,
        parent);
  }

  @Override
  public void execute(SkywarsArena arena, CommandContext context, CommandArgList args) {
    if (args.length() != 2) {
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
      return;
    }
    ArenaData data = arena.getData();
    Player player = context.getPlayer();
    Location location = player.getLocation();
    final int maxPoint = ArenaBox.Point.values().length;
    int numericalPoint = args.last().getInt();
    Preconditions.checkState(numericalPoint >= 1 && numericalPoint <= maxPoint,
        String.format("Unrecognized point. Must be between %s and %s!", 1, maxPoint));
    validateCompareToOtherArenas(arena, (other, thatBox) ->
        Preconditions.checkState(!thatBox.isWithin(location),
            "Cannot set point: location is occupied by arena",
            other.getName()));
    ArenaBox.Point point = ArenaBox.Point.ofIndex(numericalPoint - 1);
    ArenaBox thisBox = data.getBox();
    thisBox.setPoint(point, location.toVector());
    if (thisBox.isCompleted()) try {
      Location lobby = data.getLobby();
      BoundingBox thisBoundingBox = thisBox.toBoundingBox();
      Preconditions.checkState(lobby == null || !thisBox.isWithin(lobby),
          "Cannot set point: lobby spawn must be outside the arena");
      validateCompareToOtherArenas(arena, (other, thatBox) ->
          Preconditions.checkState(!thatBox.toBoundingBox().overlaps(thisBoundingBox),
              "Cannot set point: playground overlaps with the one of arena",
              other.getName()));
    } catch (Exception e) {
      thisBox.setPoint(point, null);
      throw e;
    }
    data.setWorld(player.getWorld());
    player.sendMessage(Language.getInstance().substitute(
        "{successPrefix} Updated point {0} ({1}) of {2}. (unsaved)",
        point.name(), numericalPoint, arena.getName()));
  }

  @Override
  public List<String> onTabComplete(CommandContext context, CommandArgList args) {
    List<String> suggestions = super.onTabComplete(context, args);
    if ((suggestions == null || suggestions.isEmpty()) && args.length() == 2)
      suggestions = Arrays.stream(ArenaBox.Point.values())
          .map(point -> String.valueOf(1 + point.ordinal()))
          .collect(Collectors.toList());
    return suggestions;
  }

  private void validateCompareToOtherArenas(
      SkywarsArena leftOut, BiConsumer<SkywarsArena, ArenaBox> validator) {
    for (SkywarsArena arena : Skywars.getInstance().getArenaManager()) {
      if (leftOut == arena) continue;
      final ArenaBox box = arena.getData().getBox();
      if (box.isCompleted()) validator.accept(arena, box);
    }
  }
}
