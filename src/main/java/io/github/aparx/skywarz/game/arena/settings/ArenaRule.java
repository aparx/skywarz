package io.github.aparx.skywarz.game.arena.settings;

import io.github.aparx.skywarz.game.arena.settings.rule.BooleanArenaRule;
import io.github.aparx.skywarz.game.arena.settings.rule.EnumArenaRule;
import io.github.aparx.skywarz.game.arena.settings.rule.IntArenaRule;
import io.github.aparx.skywarz.game.arena.settings.rule.AbstractArenaRule;
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
public enum ArenaRule {

  TEAM_SIZE(new IntArenaRule("team.size", 2, 1, 129)),
  WORLD_WEATHER(new EnumArenaRule<>("world.weather", WeatherType.CLEAR, WeatherType.class)),
  WORLD_TIME(new IntArenaRule("world.time", 6000, 0, 24000)),
  CHEST_REFILL(new BooleanArenaRule("refill", true)),
  PROTECTION_PHASE(new BooleanArenaRule("phase.protection", true));

  private final @NonNull AbstractArenaRule<?> rule;

}
