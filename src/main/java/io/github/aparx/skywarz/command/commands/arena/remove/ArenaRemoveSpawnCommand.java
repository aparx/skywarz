package io.github.aparx.skywarz.command.commands.arena.remove;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.language.Language;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 01:19
 * @since 1.0
 */
public class ArenaRemoveSpawnCommand extends AbstractArenaCommand {

  private static final int ARENA_ARGUMENT_INDEX = 0;
  private static final int TEAM_ARGUMENT_INDEX = 1;
  private static final int SPAWN_ARGUMENT_INDEX = 2;

  public ArenaRemoveSpawnCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
            .name("spawn")
            .args("<Arena> <Team> (ID)")
            .description("Removes a certain spawn from a team")
            .build(),
        ARENA_ARGUMENT_INDEX,
        parent);
  }

  @Override
  protected void execute(SkywarsArena arena, CommandContext context, CommandArgList args) {
    if (args.length() < 1 + TEAM_ARGUMENT_INDEX
        || args.length() > 1 + SPAWN_ARGUMENT_INDEX) {
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
      return;
    }
    TeamEnum team = args.get(TEAM_ARGUMENT_INDEX).getTeam();
    SpawnGroup spawns = arena.getData().getSpawns(team)
        .filter(Predicate.not(SpawnGroup::isEmpty))
        .orElseThrow(() -> new IllegalArgumentException(String.format(
            "There is no spawn added for team %s", team.getDefaultName())));
    if (args.length() == 1 + TEAM_ARGUMENT_INDEX) {
      int size = spawns.size();
      spawns.clear();
      context.getSender().sendMessage(Language.getInstance().substitute(
          "{successPrefix} Removed all {0} spawns in arena {1} from team {2}! (unsaved)",
          size, arena.getName(), team.getChatColor() + team.getDefaultName()));
    } else if (args.length() == 1 + SPAWN_ARGUMENT_INDEX) {
      int targetId = args.get(SPAWN_ARGUMENT_INDEX).getInt();
      Preconditions.checkState(spawns.remove(targetId) != null, String.format(
          "There is no spawn %s added for team %s", targetId, team.getDefaultName()));
      context.getSender().sendMessage(Language.getInstance().substitute(
          "{successPrefix} Removed spawn {0} in arena {1} from team {2}. (unsaved)",
          targetId, arena.getName(), team.getChatColor() + team.getDefaultName()
      ));
    }
  }

  @Override
  public List<String> onTabComplete(CommandContext context, CommandArgList args) {
    List<String> suggestions = super.onTabComplete(context, args);
    if (suggestions != null && !suggestions.isEmpty())
      return suggestions;
    if (args.length() == 1 + TEAM_ARGUMENT_INDEX)
      return Arrays.stream(TeamEnum.values())
          .filter((x) -> x.name().startsWith(args.getString(TEAM_ARGUMENT_INDEX).toUpperCase()))
          .map(TeamEnum::getDefaultName)
          .collect(Collectors.toList());
    if (args.length() == 1 + SPAWN_ARGUMENT_INDEX)
      return Optional.ofNullable(args.get(TEAM_ARGUMENT_INDEX).getTeam(null))
          .flatMap((team) -> Skywars.getInstance().getArenaManager()
              .find(args.getString(ARENA_ARGUMENT_INDEX))
              .flatMap((arena) -> arena.getData().getSpawns(team))
              .filter(Predicate.not(SpawnGroup::isEmpty)))
          .map(SpawnGroup::stream)
          .map((stream) -> {
            List<String> list = new ArrayList<>();
            stream.forEach((entry) -> list.add(String.valueOf(entry.getKey())));
            return list;
          })
          .orElse(suggestions);
    return suggestions;
  }


}
