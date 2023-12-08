package io.github.aparx.skywarz.game.scoreboard;

import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-08 12:06
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum MatchScoreboard {

  IDLE(new GameScoreboard(
      MatchState.IDLE, TickDuration.ofSecond(), "    §lLobby§r    ",
      List.of(
          StringUtils.SPACE,
          "Map:",
          "§b{match.arena}",
          StringUtils.SPACE,
          "Kit:",
          "{player.kit.displayName}"
      ))),

  PLAYING_ALIVE(new GameScoreboard(
      MatchState.PLAYING, "alive", TickDuration.ofSecond(), " §b§lSKYWARZ§r ",
      List.of(
          StringUtils.SPACE,
          "Map:",
          "§b{match.arena}",
          StringUtils.SPACE,
          "Time:",
          "§e{match.time.left.format}",
          StringUtils.SPACE,
          "Alive:",
          "§e{match.alive}",
          StringUtils.SPACE,
          "Kills:",
          "§c-"
      ))),

  PLAYING_DEAD(new GameScoreboard(
      MatchState.PLAYING, "dead", TickDuration.ofSecond(), "  §b§lSKYWARZ§r  ",
      List.of(
          StringUtils.SPACE,
          "Map:",
          "§b{match.arena}",
          StringUtils.SPACE,
          "Time:",
          "§e{match.time.left.format}",
          StringUtils.SPACE,
          "Alive:",
          "§e{match.alive}",
          StringUtils.SPACE,
          "Place:",
          "§c13th"
      )));

  private final @NonNull GameScoreboard scoreboard;

}
