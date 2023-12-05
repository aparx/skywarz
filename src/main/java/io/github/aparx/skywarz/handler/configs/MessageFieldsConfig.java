package io.github.aparx.skywarz.handler.configs;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.bufig.handler.ConfigHandler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 09:20
 * @since 1.0
 */
@Getter
@Deprecated(forRemoval = true)
@SuppressWarnings("FieldMayBeFinal")
public abstract class MessageFieldsConfig extends ConfigObject {

  // MESSAGES BEGIN

  @ConfigMapping("prefix.default")
  private String prefix = ChatColor.AQUA + "[Skywarz]" + ChatColor.RESET;

  @ConfigMapping("prefix.warning")
  private String warningPrefix = ChatColor.RED + "[Skywarz]";

  @ConfigMapping("prefix.success")
  private String successPrefix = ChatColor.GREEN + "[Skywarz]";

  @ConfigMapping("log.loaded")
  private String loadedLog = "{successPrefix} Skywarz has been loaded";

  @ConfigMapping("log.unloaded")
  private String unloadedLog = "{successPrefix} Skywarz has been unloaded";

  // ERRORS BEGIN

  @ConfigMapping("errors.error")
  private String error = "{warningPrefix} Error: {message}";

  @ConfigMapping("errors.not a number")
  private String errorNotNumber = "{warningPrefix} Value {value} is not a number!";

  @ConfigMapping("errors.not an integer")
  private String errorNotInteger = "{warningPrefix} Value {value} is not an integer!";

  @ConfigMapping("errors.negative number")
  private String errorNotPositive = "{warningPrefix} Value {value} must be positive!";

  @ConfigMapping("errors.self not a player")
  private String errorSelfNotPlayer = "{warningPrefix} Be a player to perform this action!";

  @ConfigMapping("errors.command syntax")
  private String errorCommandSyntax = "{warningPrefix} Syntax error: {usage}";

  @ConfigMapping("errors.command not found")
  private String errorCommandNotFound = "{warningPrefix} Command not found, try §r/{label} help";

  @ConfigMapping("errors.missing permission")
  private String errorPermission = "{warningPrefix} Missing permission to perform this action!";

  @ConfigMapping("errors.in a match")
  private String errorInMatch = "{warningPrefix} You are already in a match!";

  @ConfigMapping("errors.not in a match")
  private String errorNotInMatch = "{warningPrefix} You are not in a match!";

  @ConfigMapping("errors.cannot join match")
  private String errorJoinMatch = "{warningPrefix} You cannot join this match.";

  @ConfigMapping("errors.arena not found")
  private String errorArenaNotFound = "{warningPrefix} Arena {0} not found!";
  // ERRORS END

  // SUCCESS START
  @ConfigMapping("match.join.success")
  private String successJoinMatch = "{successPrefix} You joined the match!";

  @ConfigMapping("match.leave.success")
  private String successLeaveMatch = "{successPrefix} You left the match!";
  // SUCCESS END

  // MATCH START
  @ConfigMapping("match.broadcast.joined")
  private String matchPlayerJoined = "§b[+]§7 Player §r{player.name}§7 joined the game!";

  @ConfigMapping("match.broadcast.left")
  private String matchPlayerLeft = "§c[-]§7 Player §r{player.name}§7 left the game!";

  @ConfigMapping("match.broadcast.need more players")
  private String matchBroadcastPlayersRequired =
      "{warningPrefix} §7Need §c{missing}§7 more players!";

  @ConfigMapping("match.broadcast.start")
  private String matchBroadcastStart = "{prefix} §7The match starts in §b{time}§r seconds!";

  @ConfigMapping("match.team.switch.success")
  private String matchTeamSwitchedSuccess = "{successPrefix} You joined Team {color}{name}§a!";

  @ConfigMapping("match.team.switch.error")
  private String matchTeamSwitchedError = "{warningPrefix} You cannot join this team!";
  // MATCH END

  // MENU START
  @ConfigMapping("menu.team selector.title")
  private String teamSelectorTitle = "You cannot join this team!";
  // MENU END

  // MESSAGES END

  public MessageFieldsConfig(@NonNull String configId, @NonNull ConfigHandler<?> handler) {
    super(configId, handler);
  }

}
