package io.github.aparx.skywarz.command.commands.arena.add;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaSpawnCommand;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.handler.configs.Language;
import io.github.aparx.skywarz.skywars.arena.Arena;
import io.github.aparx.skywarz.skywars.team.TeamEnum;
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
        parent, ARENA_ARGUMENT_INDEX);
  }

  @Override
  protected void setLocation(
      Location location, Arena arena, CommandContext context, CommandArgList args) {
    if (args.length() != 1 + TEAM_ARGUMENT_INDEX)
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      Player player = context.getPlayer();
      TeamEnum team = args.get(TEAM_ARGUMENT_INDEX).getTeam();
      int spawnId = arena.createSpawnsIfAbsent(team).add(location);
      player.sendMessage(Language.getLanguage().substitute(
          "{successPrefix} Added spawn with ID {0} in arena {1} to team {2}",
          spawnId,
          arena.getName(),
          team.getColor() + team.getDefaultName()
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
