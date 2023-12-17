package io.github.aparx.skywarz.command.skeleton;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 02:50
 * @since 1.0
 */
public interface CommandNodeExecutor {

  void execute(CommandContext context, CommandArgList args);

  default List<String> onTabComplete(CommandContext context, CommandArgList args) {
    return null;
  }

  static List<String> getOnlinePlayerSuggestions(CommandContext context, String argument) {
    boolean isPlayer = context.isPlayer();
    return Bukkit.getOnlinePlayers().stream()
        .filter((x) -> !isPlayer || ((Player) context.getSender()).canSee(x))
        .map(Player::getName)
        .filter((name) -> StringUtils.startsWithIgnoreCase(name, argument))
        .collect(Collectors.toList());
  }

}
