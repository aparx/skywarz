package io.github.aparx.skywarz.game.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
  SOLO(ChatColor.WHITE, Material.WHITE_CONCRETE),

  RED(ChatColor.RED, Material.RED_CONCRETE),
  GREEN(ChatColor.GREEN, Material.GREEN_CONCRETE),
  BLUE(ChatColor.BLUE, Material.BLUE_CONCRETE),
  WHITE(ChatColor.WHITE, Material.WHITE_CONCRETE),
  BLACK(ChatColor.BLACK, Material.BLACK_CONCRETE);

  private final @NonNull ChatColor color;
  private final @NonNull Material material;

  private final String defaultName = StringUtils.capitalize(name().toLowerCase());

}
