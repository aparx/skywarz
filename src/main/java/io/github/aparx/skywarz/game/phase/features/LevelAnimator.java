package io.github.aparx.skywarz.game.phase.features;

import io.github.aparx.skywarz.game.phase.SkywarsPhase;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.experimental.UtilityClass;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 08:27
 * @since 1.0
 */
@UtilityClass
public final class LevelAnimator {

  public static void animate(SkywarsPhase phase, int secondsLeft) {
    long duration = phase.getDuration().toTicks();
    long elapsed = phase.getTicker().getElapsed(TimeUnit.TICKS);
    float exp = calcExp(duration, elapsed);
    phase.getMatch().getAudience().entity().forEach((player) -> {
      player.setExp(exp);
      player.setLevel(secondsLeft);
    });
  }

  static float calcExp(long duration, long elapsed) {
    return Math.max(Math.min(1.0F - ((1F + elapsed) / duration), 1.0F), 0.0F);
  }

}
