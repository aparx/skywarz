package io.github.aparx.skywarz.game.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 05:23
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum TeamEnum {

  /** This team is a special one, whereas this is used for purely solo Skywars arenas */
  SOLO(ChatColor.WHITE, DyeColor.WHITE),

  RED(ChatColor.RED, DyeColor.RED),
  GREEN(ChatColor.GREEN, DyeColor.GREEN),
  BLUE(ChatColor.BLUE, DyeColor.LIGHT_BLUE),
  WHITE(ChatColor.WHITE, DyeColor.WHITE),
  BLACK(ChatColor.BLACK, DyeColor.BLACK),
  CYAN(ChatColor.AQUA, DyeColor.CYAN),
  ORANGE(ChatColor.GOLD, DyeColor.ORANGE),
  YELLOW(ChatColor.YELLOW, DyeColor.YELLOW),
  PURPLE(ChatColor.DARK_PURPLE, DyeColor.PURPLE),
  MAGENTA(ChatColor.LIGHT_PURPLE, DyeColor.MAGENTA),
  LIME(ChatColor.GREEN, DyeColor.LIME),
  BROWN(ChatColor.GRAY, DyeColor.BROWN),
  GRAY(ChatColor.GRAY, DyeColor.LIGHT_GRAY);

  private final @NonNull ChatColor chatColor;
  private final @NonNull DyeColor dyeColor;

  private final String defaultName = StringUtils.capitalize(name().toLowerCase());

}
