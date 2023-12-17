package io.github.aparx.skywarz.game.arena.settings;

import io.github.aparx.skywarz.game.arena.settings.rule.BooleanGameRule;
import io.github.aparx.skywarz.game.arena.settings.rule.EnumGameRule;
import io.github.aparx.skywarz.game.arena.settings.rule.IntGameRule;
import io.github.aparx.skywarz.game.arena.settings.rule.SkywarsGameRule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.WeatherType;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-17 12:23
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum SkywarsGameRules {

  TEAM_SIZE(new IntGameRule("teamSize", 2, 1, 129)),
  WORLD_WEATHER(new EnumGameRule<>("weather", null, WeatherType.class)),
  WORLD_TIME(new IntGameRule("time", 6000, 0)),
  CHEST_REFILL(new BooleanGameRule("refill", true));

  private final @NonNull SkywarsGameRule<?> gameRule;

}
