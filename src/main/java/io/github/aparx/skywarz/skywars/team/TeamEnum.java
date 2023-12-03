package io.github.aparx.skywarz.skywars.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 05:23
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum TeamEnum {

  RED(ChatColor.RED),
  GREEN(ChatColor.GREEN),
  BLUE(ChatColor.BLUE),
  WHITE(ChatColor.WHITE),
  BLACK(ChatColor.BLACK);

  private final @NonNull ChatColor color;

  private final String defaultName = StringUtils.capitalize(name().toLowerCase());

}
