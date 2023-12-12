package io.github.aparx.skywarz.command.commands.arena.add;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaSpawnCommand;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.arena.ArenaBox;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.language.Language;
import org.bukkit.Location;
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
public class ArenaAddSpawnCommand extends AbstractArenaSpawnCommand {

  private static final int ARENA_ARGUMENT_INDEX = 0;
  private static final int TEAM_ARGUMENT_INDEX = 1;

  public ArenaAddSpawnCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
            .name("spawn")
            .args("<Arena> <Team>")
            .description(
                "Adds a spawn to a team (independent from team size)")
            .build(),
        ARENA_ARGUMENT_INDEX,
        parent);
  }

  @Override
  protected void setLocation(
      Location location, SkywarsArena arena, CommandContext context, CommandArgList args) {
    if (args.length() != 1 + TEAM_ARGUMENT_INDEX)
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      ArenaBox box = arena.getData().getBox();
      Preconditions.checkState(box.isCompleted(), "Set the playground points first");
      Preconditions.checkState(box.isWithin(location), "Spawn is not within the playground");

      Player player = context.getPlayer();
      TeamEnum team = args.get(TEAM_ARGUMENT_INDEX).getTeam();
      int spawnId = arena.getData().createSpawnsIfAbsent(team).add(location);
      player.sendMessage(Language.getInstance().substitute(
          "{successPrefix} Added spawn with ID {0} in arena {1} to team {2}. (unsaved)",
          spawnId, arena.getName(), team.getChatColor() + team.getDefaultName()
      ));
    }
  }

  @Override
  public List<String> onTabComplete(CommandContext context, CommandArgList args) {
    List<String> suggestions = super.onTabComplete(context, args);
    if ((suggestions == null || suggestions.isEmpty())
        && args.length() == 1 + TEAM_ARGUMENT_INDEX)
      suggestions = Arrays.stream(TeamEnum.values())
          .filter((x) -> x.name().startsWith(args.getString(TEAM_ARGUMENT_INDEX).toUpperCase()))
          .map(TeamEnum::getDefaultName)
          .collect(Collectors.toList());
    return suggestions;
  }
}
