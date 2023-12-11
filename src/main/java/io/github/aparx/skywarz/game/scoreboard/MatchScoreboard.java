package io.github.aparx.skywarz.game.scoreboard;

import io.github.aparx.skywarz.game.match.SkywarsMatchState;
import io.github.aparx.skywarz.game.scoreboard.playing.PlayingScoreboard;
import io.github.aparx.skywarz.utils.tick.TickDuration;
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
      SkywarsMatchState.IDLE, TickDuration.ofSecond(), "    §lLobby§r    ",
      List.of(
          StringUtils.SPACE,
          "Map:",
          "§b{match.arena}",
          StringUtils.SPACE,
          "Kit:",
          "{player.kit.displayName}"
      ))),

  PLAYING_ALIVE(new PlayingScoreboard(
      SkywarsMatchState.PLAYING, "alive", TickDuration.ofSecond(), " §b§lSKYWARZ§r ",
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
          "§c{player.match.stats.kills}"
      ))),

  PLAYING_DEAD(new PlayingScoreboard(
      SkywarsMatchState.PLAYING, "dead", TickDuration.ofSecond(), "  §b§lSKYWARZ§r  ",
      List.of(
          StringUtils.SPACE,
          "Map:",
          "§b{match.arena}",
          StringUtils.SPACE,
          "Time:",
          "§e{match.time.left.format}",
          StringUtils.SPACE,
          "Alive:",
          "§e{match.alive}"
      ))),

  DONE(new GameScoreboard(
      SkywarsMatchState.DONE, TickDuration.ofSecond(), "  §b§lSKYWARZ§r  ",
      List.of(
          StringUtils.SPACE,
          "Map:",
          "§b{match.arena}",
          StringUtils.SPACE,
          "Winner:",
          "{match.winner.color}{match.winner.name}",
          StringUtils.SPACE,
          "§7~§oby aparx"
      )));

  private final @NonNull GameScoreboard scoreboard;

}
