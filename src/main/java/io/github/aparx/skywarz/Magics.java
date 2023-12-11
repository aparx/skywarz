package io.github.aparx.skywarz;

import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.experimental.UtilityClass;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 02:33
 * @since 1.0
 */
@UtilityClass
public final class Magics {

  /** Boolean that determines whether a match is actually winnable when one team is left */
  public static final boolean GAME_WINNABLE = !isDevelopment();

  // TODO move to game settings or main?
  public static final TickDuration DEV_PLAYING_DURATION = TickDuration.of(TimeUnit.MINUTES, 1);

  public static final TickDuration DEV_IDLE_DURATION = TickDuration.of(TimeUnit.SECONDS, 3);

  /** Returns whether Skywarz is run in development mode */
  private static final boolean DEVELOPMENT = false;

  public static boolean isDevelopment() {
    // use a getter to avoid "PointlessArithmeticExpression" and for future changes
    return DEVELOPMENT;
  }

}
