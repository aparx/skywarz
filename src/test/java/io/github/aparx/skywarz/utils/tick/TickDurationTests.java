package io.github.aparx.skywarz.utils.tick;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 05:07
 * @since 1.0
 */
public class TickDurationTests {

  @Test
  public void getAmount() {
    TickDuration duration = TickDuration.of(TimeUnit.MINUTES, 1);
    Assertions.assertEquals(0, duration.getAmount(TimeUnit.HOURS));
    Assertions.assertEquals(1, duration.getAmount(TimeUnit.MINUTES));
    Assertions.assertEquals(60, duration.getAmount(TimeUnit.SECONDS));
    Assertions.assertEquals(TimeUnit.MINUTES.toTicks(), duration.getAmount(TimeUnit.TICKS));
  }

}
