package io.github.aparx.skywarz.game.phase.features;

import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 08:27
 * @since 1.0
 */
@UtilityClass
public final class LevelAnimator {

  public static void animate(GamePhase phase, int secondsLeft) {
    long duration = phase.getDuration().toTicks();
    long elapsed = phase.getTicker().getElapsed(TimeUnit.TICKS);
    float exp = calcExp(duration, elapsed);
    phase.getMatch().getAudience().stream()
        .map(SkywarsPlayer::findOnline)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach((player) -> {
          player.setExp(exp);
          player.setLevel(secondsLeft);
        });
  }

  static float calcExp(long duration, long elapsed) {
    return Math.max(Math.min(1.0F - ((1F + elapsed) / duration), 1.0F), 0.0F);
  }

}
