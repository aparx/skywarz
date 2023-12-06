package io.github.aparx.skywarz.language;

import io.github.aparx.bufig.ArrayPath;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

import java.util.LinkedHashMap;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 10:21
 * @since 1.0
 */
@UtilityClass
public final class MessageKeys {

  public static final ArrayPath PREFIX = ArrayPath.of("prefix");
  public static final ArrayPath SUCCESS_PREFIX = ArrayPath.of("successPrefix");
  public static final ArrayPath WARNING_PREFIX = ArrayPath.of("warningPrefix");

  @UtilityClass
  public static final class Errors {
    public static final ArrayPath GENERIC = ArrayPath.of("errors.generic");

    public final ArrayPath PLAYER = ArrayPath.of("errors.not a player");
    public final ArrayPath INTEGER = ArrayPath.of("errors.not an integer");
    public final ArrayPath NUMBER = ArrayPath.of("errors.not a number");
    public final ArrayPath SYNTAX = ArrayPath.of("errors.syntax");
    public final ArrayPath PERMISSION = ArrayPath.of("errors.permission");

    public final ArrayPath ARENA_NOT_FOUND = ArrayPath.of("errors.arena not found");
    public final ArrayPath COMMAND_NOT_FOUND = ArrayPath.of("errors.command not found");

    public final ArrayPath IN_A_MATCH = ArrayPath.of("errors.in a match");
    public final ArrayPath NOT_IN_A_MATCH = ArrayPath.of("errors.not in a match");
    public final ArrayPath MATCH_IS_FULL = ArrayPath.of("errors.match is full");
  }

  @UtilityClass
  public static final class Match {
    public final ArrayPath JOIN_SUCCESS = ArrayPath.of("match.join.success");
    public final ArrayPath JOIN_ERROR = ArrayPath.of("match.join.error");
    public final ArrayPath JOIN_BROADCAST = ArrayPath.of("match.join.broadcast");

    public final ArrayPath LEAVE_SUCCESS = ArrayPath.of("match.leave.success");
    public final ArrayPath LEAVE_BROADCAST = ArrayPath.of("match.leave.broadcast");

    public final ArrayPath TEAM_SWITCH_SUCCESS = ArrayPath.of("match.team switch.success");
    public final ArrayPath TEAM_SWITCH_ERROR = ArrayPath.of("match.team switch.error");

    public final ArrayPath BROADCAST_START = ArrayPath.of("match.broadcast.start");
    public final ArrayPath BROADCAST_REQUIRE = ArrayPath.of("match.broadcast.require players");
    public final ArrayPath BROADCAST_CLOSING = ArrayPath.of("match.broadcast.closing");

    public final ArrayPath PRIORITY_ERROR = ArrayPath.of("match.priority.error");
    public final ArrayPath PRIORITY_KICK = ArrayPath.of("match.priority.kick");

    public final ArrayPath ERROR_DEQUEUED = ArrayPath.of("match.dequeued");

    public final ArrayPath KILLED = ArrayPath.of("match.killed");
    public final ArrayPath DIED = ArrayPath.of("match.death");

    public final ArrayPath QUICKSTART_SUCCESS = ArrayPath.of("match.quickstart.success");
    public final ArrayPath QUICKSTART_ERROR = ArrayPath.of("match.quickstart.error");
  }

  static final LinkedHashMap<ArrayPath, String> defaultMessages;

  static {
    LinkedHashMap<ArrayPath, String> map = new LinkedHashMap<>();
    map.put(PREFIX, ChatColor.AQUA + "[Skywarz]" + ChatColor.RESET);
    map.put(SUCCESS_PREFIX, ChatColor.GREEN + "[Skywarz]");
    map.put(WARNING_PREFIX, ChatColor.RED + "[Skywarz]");

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

    map.put(Match.JOIN_SUCCESS, "{successPrefix} You joined the match!");
    map.put(Match.JOIN_ERROR, "{warningPrefix} You cannot join this match!");
    map.put(Match.JOIN_BROADCAST, "§a[-]§7 Player §r{player.name}§7 has joined the game.");
    map.put(Match.LEAVE_SUCCESS, "{successPrefix} You left the match!");
    map.put(Match.LEAVE_BROADCAST, "§c[-]§7 Player §r{player.name}§7 has left the game.");
    map.put(Match.TEAM_SWITCH_SUCCESS, "{successPrefix} You joined Team {team.color}{team.name}§a!");
    map.put(Match.TEAM_SWITCH_ERROR, "{warningPrefix} You cannot join this team!");
    map.put(Match.PRIORITY_ERROR, "{warningPrefix} Match is full. Could not find anyone that is kickable!");
    map.put(Match.PRIORITY_KICK, "{warningPrefix} You have been kicked for someone that is VIP.");
    map.put(Match.ERROR_DEQUEUED, "{warningPrefix} An error occurred. You have been dequeued.");

    map.put(Match.BROADCAST_START, "{prefix}§7 The game starts in §b{time}§7 seconds!");
    map.put(Match.BROADCAST_REQUIRE, "{prefix}§7 Require §c{missing}§7 more players to start!");
    map.put(Match.BROADCAST_CLOSING, "{prefix}§7 The match closes in {time} seconds!");

    map.put(Match.QUICKSTART_SUCCESS, "{successPrefix} The game will start shortly.");
    map.put(Match.QUICKSTART_ERROR, "{warningPrefix} Cannot quickstart your match.");

    map.put(Match.KILLED, "{prefix}§7 Player {player.team.color}{player.name}§7 was slained by {killer.team.color}{killer.name}§7!");
    map.put(Match.DIED, "{prefix}§7 Player {player.team.color}{player.name}§7 died!");

    defaultMessages = map;
  }

}
