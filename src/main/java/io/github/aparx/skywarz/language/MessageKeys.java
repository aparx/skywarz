package io.github.aparx.skywarz.language;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.game.match.GameMatchState;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 10:21
 * @since 1.0
 */
@UtilityClass
public final class MessageKeys {

  public static final ArrayPath PREFIX = createKey("prefix");
  public static final ArrayPath SUCCESS_PREFIX = createKey("successPrefix");
  public static final ArrayPath WARNING_PREFIX = createKey("warningPrefix");

  @UtilityClass
  public static final class Errors {
    public static final ArrayPath GENERIC = createKey("errors.generic");

    public final ArrayPath PLAYER = createKey("errors.not a player");
    public final ArrayPath INTEGER = createKey("errors.not an integer");
    public final ArrayPath NUMBER = createKey("errors.not a number");
    public final ArrayPath SYNTAX = createKey("errors.syntax");
    public final ArrayPath PERMISSION = createKey("errors.permission");

    public final ArrayPath ARENA_NOT_FOUND = createKey("errors.arena not found");
    public final ArrayPath COMMAND_NOT_FOUND = createKey("errors.command not found");

    public final ArrayPath IN_A_MATCH = createKey("errors.in a match");
    public final ArrayPath NOT_IN_A_MATCH = createKey("errors.not in a match");
    public final ArrayPath MATCH_IS_FULL = createKey("errors.match is full");
  }

  @UtilityClass
  public static final class Match {
    public final ArrayPath JOIN_ERROR = createKey("match.join.error");
    public final ArrayPath JOIN_BROADCAST = createKey("match.join.broadcast");

    public final ArrayPath LEAVE_SUCCESS = createKey("match.leave.success");
    public final ArrayPath LEAVE_BROADCAST = createKey("match.leave.broadcast");

    public final ArrayPath TEAM_SWITCH_ERROR = createKey("match.team switch.error");

    public final ArrayPath BROADCAST_START = createKey("match.broadcast.start");
    public final ArrayPath BROADCAST_REQUIRE = createKey("match.broadcast.require players");
    public final ArrayPath BROADCAST_CLOSING = createKey("match.broadcast.closing");

    public final ArrayPath PRIORITY_ERROR = createKey("match.priority.error");
    public final ArrayPath PRIORITY_KICK = createKey("match.priority.kick");

    public final ArrayPath KIT_SELECTION = createKey("match.kit.select");
    public final ArrayPath KIT_ASSIGN = createKey("match.kit.assign");

    public final ArrayPath ERROR_DEQUEUED = createKey("match.dequeued");

    public final ArrayPath KILLED = createKey("match.killed");
    public final ArrayPath DIED = createKey("match.death");

    public final ArrayPath QUICKSTART_SUCCESS = createKey("match.quickstart.success");
    public final ArrayPath QUICKSTART_ERROR = createKey("match.quickstart.error");

    public final ArrayPath TITLE_YOU_WON = createKey("match.finish.title.you won");
    public final ArrayPath TITLE_YOU_LOST = createKey("match.finish.title.you lost");
    public final ArrayPath TITLE_TEAM_WON = createKey("match.finish.title.team won");
    public final ArrayPath TEAM_WON = createKey("match.finish.broadcast.team won");

    public final ArrayPath CHAT_TEAM = createKey("match.chat.team");
    public final ArrayPath CHAT_ALL = createKey("match.chat.all");
    public final ArrayPath CHAT_SPECTATOR = createKey("match.chat.spectator");

  }

  @UtilityClass
  public static final class Stats {

    public final ArrayPath FETCHING = createKey("stats.fetching");

    public final ArrayPath NONE_OTHER = createKey("stats.none other");
    public final ArrayPath NONE_SELF = createKey("stats.none self");

    public final ArrayPath OVERVIEW = createKey("stats.overview");

  }

  static final LinkedHashMap<ArrayPath, Object> defaultMessages;

  private static final EnumMap<TimeUnit, ArrayPath> pluralTimeUnitKeys =
      new EnumMap<>(TimeUnit.class);

  private static final EnumMap<TimeUnit, ArrayPath> singularTimeUnitKeys =
      new EnumMap<>(TimeUnit.class);

  private static final EnumMap<GameMatchState, ArrayPath> matchStateKeys =
      new EnumMap<>(GameMatchState.class);

  private static final EnumMap<TeamEnum, ArrayPath> teamNameKeys =
      new EnumMap<>(TeamEnum.class);

  static {
    Arrays.stream(TimeUnit.values()).forEach((unit) -> {
      ArrayPath time = ArrayPath.of("time", unit.name());
      pluralTimeUnitKeys.put(unit, time.add("plural"));
      singularTimeUnitKeys.put(unit, time.add("singular"));
    });
    Arrays.stream(GameMatchState.values()).forEach((state) -> {
      matchStateKeys.put(state, ArrayPath.of("state", state.name()));
    });
    Arrays.stream(TeamEnum.values()).forEach((team) -> {
      teamNameKeys.put(team, ArrayPath.of("team", team.name()));
    });
  }

  public static ArrayPath getTimeUnitKey(@NonNull TimeUnit unit, boolean plural) {
    Preconditions.checkNotNull(unit, "Unit must not be null");
    return plural ? pluralTimeUnitKeys.get(unit) : singularTimeUnitKeys.get(unit);
  }

  public static ArrayPath getMatchStateKey(@NonNull GameMatchState state) {
    Preconditions.checkNotNull(state, "State must not be null");
    return matchStateKeys.get(state);
  }

  public static ArrayPath getTeamKey(@NonNull TeamEnum teamEnum) {
    Preconditions.checkNotNull(teamEnum, "Team must not be null");
    return teamNameKeys.get(teamEnum);
  }

  private static ArrayPath createKey(String path) {
    return ArrayPath.parse(path);
  }

  static {
    LinkedHashMap<ArrayPath, Object> map = new LinkedHashMap<>();
    map.put(PREFIX, ChatColor.AQUA + "[Skywarz]" + ChatColor.RESET);
    map.put(SUCCESS_PREFIX, ChatColor.GREEN + "[Skywarz]");
    map.put(WARNING_PREFIX, ChatColor.RED + "[Skywarz]");

    // @formatter:off
    map.put(Errors.GENERIC, "{warningPrefix} Error: {0}");
    map.put(Errors.SYNTAX, "{warningPrefix} Syntax: {usage}");
    map.put(Errors.IN_A_MATCH, "{warningPrefix} You are in a match already!");
    map.put(Errors.NOT_IN_A_MATCH, "{warningPrefix} You are not in a match!");
    map.put(Errors.NUMBER, "{warningPrefix} Value {0} is not a number!");
    map.put(Errors.INTEGER, "{warningPrefix} Value {0} is not an integer!");
    map.put(Errors.ARENA_NOT_FOUND, "{warningPrefix} Cannot find arena {0}!");
    map.put(Errors.PERMISSION, "{warningPrefix} You do not have the permission for this action!");
    map.put(Errors.COMMAND_NOT_FOUND, "{warningPrefix} Command not found. Try §r/sw help§c.");
    map.put(Errors.MATCH_IS_FULL, "{warningPrefix} Cannot join: Match is full!");
    map.put(Errors.PLAYER, "{warningPrefix} Argument is not a valid player!");

    map.put(Match.JOIN_ERROR, "{warningPrefix} You cannot join this match!");
    map.put(Match.JOIN_BROADCAST, "§a[-]§7 Player §r{player.name}§7 has joined the game.");
    map.put(Match.LEAVE_SUCCESS, "{successPrefix} You left the match!");
    map.put(Match.LEAVE_BROADCAST, "§c[-]§7 Player §r{player.name}§7 has left the game.");
    map.put(Match.TEAM_SWITCH_ERROR, "{warningPrefix} You cannot join this team!");
    map.put(Match.PRIORITY_ERROR, "{warningPrefix} Match is full. Could not find anyone that is kickable!");
    map.put(Match.PRIORITY_KICK, "{warningPrefix} You have been kicked for someone that is VIP.");
    map.put(Match.ERROR_DEQUEUED, "{warningPrefix} An error occurred. You have been dequeued.");

    map.put(Match.BROADCAST_START, "{prefix}§7 The game starts in §8{match.time.left.literal}§7!");
    map.put(Match.BROADCAST_REQUIRE, "{prefix}§7 Require §c{match.missing}§7 more players to start!");
    map.put(Match.BROADCAST_CLOSING, "{prefix}§7 The match closes in {match.time.left.literal}!");

    map.put(Match.QUICKSTART_SUCCESS, "{successPrefix} The game will start shortly.");
    map.put(Match.QUICKSTART_ERROR, "{warningPrefix} Cannot quickstart your match.");

    map.put(Match.KILLED, "{prefix}§7 Player {player.team.color}{player.name}§7 was killed by {killer.team.color}{killer.name}§7!");
    map.put(Match.DIED, "{prefix}§7 Player {player.team.color}{player.name}§7 died!");

    map.put(Match.KIT_SELECTION, "{successPrefix} You selected the Kit {player.kit.displayName}§a!");
    map.put(Match.KIT_ASSIGN, "{successPrefix} You have been assigned the Kit {player.kit.displayName}§a!");

    map.put(Match.TITLE_YOU_WON, "§aYou won!");
    map.put(Match.TITLE_YOU_LOST, "§cYou lost!");
    map.put(Match.TITLE_TEAM_WON, "Team {team.displayName}§r won!");
    map.put(Match.TEAM_WON, List.of(
        "{team.color}[Skywarz] Team §l{team.displayName} won!",
        "{team.color}[Skywarz] Thanks for using Skywarz!"
    ));

    map.put(Match.CHAT_TEAM, "{sender.team.color}[TEAM] §r{sender.name}: §7{message}");
    map.put(Match.CHAT_ALL, "§8[ALL] {sender.team.color}{sender.name}: §7{message}");
    map.put(Match.CHAT_SPECTATOR, "§8§o[DEAD] §7{sender.name}: §7{message}");

    map.put(Stats.FETCHING, "{prefix} §7Fetching player's stats...");
    map.put(Stats.NONE_OTHER, "{warningPrefix} Player {target.name} has no stats in Skywarz");
    map.put(Stats.NONE_SELF, "{warningPrefix} You have stats in Skywarz");
    map.put(Stats.OVERVIEW, List.of(
        "{prefix} §7Stats for §8{target.name}§7:",
        "{prefix} §8• §7Points: §e{target.total.points}",
        "{prefix} §8• §7Played: §b{target.total.played}",
        "{prefix} §8• §7Won: §b{target.total.won}",
        "{prefix} §8• §7Kills: §b{target.total.kills}",
        "{prefix} §8• §7Deaths: §b{target.total.deaths}",
        "{prefix} §8• §7K/D: §b{target.total.kd}",
        "{prefix} §8• §7Win%: §b{target.total.winChance}"
    ));

    for (TimeUnit unit : TimeUnit.values()) {
      final String name = unit.name().toLowerCase();
      map.put(getTimeUnitKey(unit, true), name);
      map.put(getTimeUnitKey(unit, false), name.substring(0, name.length() - 1));
    }

    for (GameMatchState state : GameMatchState.values())
      map.put(getMatchStateKey(state), StringUtils.capitalize(state.name().toLowerCase()));

    for (TeamEnum teamEnum : TeamEnum.values())
      map.put(getTeamKey(teamEnum), teamEnum.getDefaultName());

    defaultMessages = map;
  }

}
