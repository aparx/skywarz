package io.github.aparx.skywarz.game.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
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
  SOLO(Color.TEAL, ChatColor.WHITE, DyeColor.WHITE),

  RED(Color.RED, ChatColor.RED, DyeColor.RED),
  GREEN(Color.GREEN, ChatColor.GREEN, DyeColor.GREEN),
  BLUE(Color.BLUE, ChatColor.BLUE, DyeColor.BLUE),
  WHITE(Color.WHITE, ChatColor.WHITE, DyeColor.WHITE),
  BLACK(Color.BLACK, ChatColor.BLACK, DyeColor.BLACK),
  CYAN(Color.AQUA, ChatColor.AQUA, DyeColor.CYAN),
  ORANGE(Color.ORANGE, ChatColor.GOLD, DyeColor.ORANGE),
  YELLOW(Color.YELLOW, ChatColor.YELLOW, DyeColor.YELLOW),
  PURPLE(Color.PURPLE, ChatColor.DARK_PURPLE, DyeColor.PURPLE),
  MAGENTA(Color.PURPLE, ChatColor.LIGHT_PURPLE, DyeColor.MAGENTA),
  LIME(Color.LIME, ChatColor.GREEN, DyeColor.LIME),
  BROWN(Color.GRAY, ChatColor.GRAY, DyeColor.BROWN),
  GRAY(Color.GRAY, ChatColor.GRAY, DyeColor.LIGHT_GRAY);

  private final @NonNull Color color;
  private final @NonNull ChatColor chatColor;
  private final @NonNull DyeColor dyeColor;

  private final String defaultName = StringUtils.capitalize(name().toLowerCase());

}
