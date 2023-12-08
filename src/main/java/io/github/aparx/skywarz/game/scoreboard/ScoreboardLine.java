package io.github.aparx.skywarz.game.scoreboard;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-08 10:38
 * @since 1.0
 */
@FunctionalInterface
public interface ScoreboardLine {

  @Nullable String create();

  static ScoreboardLine of(@Nullable String line) {
    return () -> line;
  }

}
