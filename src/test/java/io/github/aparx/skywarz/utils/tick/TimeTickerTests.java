package io.github.aparx.skywarz.utils.tick;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 05:07
 * @since 1.0
 */
public class TimeTickerTests {

  @Test
  public void getElapsed() {
    TimeTicker ticker = new TimeTicker();
    ticker.set(TimeUnit.DAYS.toTicks());
    Assertions.assertEquals(1, ticker.getElapsed(TimeUnit.DAYS));
    Assertions.assertEquals(TimeUnit.DAYS.toTicks(), ticker.getElapsed());
    Assertions.assertEquals(TimeUnit.DAYS.toTicks(), ticker.getElapsed(TimeUnit.TICKS));
    Assertions.assertEquals(
        TimeUnit.DAYS.toTicks() / TimeUnit.SECONDS.toTicks(),
        ticker.getElapsed(TimeUnit.SECONDS));
    Assertions.assertEquals(
        TimeUnit.DAYS.toTicks() / TimeUnit.MINUTES.toTicks(),
        ticker.getElapsed(TimeUnit.MINUTES));
    Assertions.assertEquals(
        TimeUnit.DAYS.toTicks() / TimeUnit.HOURS.toTicks(),
        ticker.getElapsed(TimeUnit.HOURS));

    ticker = new TimeTicker(TickDuration.of(TimeUnit.SECONDS, 2));
    ticker.set(1);
    Assertions.assertEquals(2, ticker.getElapsed(TimeUnit.SECONDS));
    Assertions.assertEquals(2, ticker.getElapsed(TimeUnit.SECONDS));
    Assertions.assertEquals(40, ticker.getElapsed(TimeUnit.TICKS));
  }

  @Test
  public void hasElapsed() {
    TimeTicker ticker = new TimeTicker();
    ticker.set(TimeUnit.MINUTES.toTicks());
    Assertions.assertFalse(ticker.hasElapsed(TimeUnit.DAYS.toTicks()));
    Assertions.assertFalse(ticker.hasElapsed(TimeUnit.HOURS.toTicks()));
    Assertions.assertTrue(ticker.hasElapsed(TimeUnit.MINUTES.toTicks()));
    Assertions.assertTrue(ticker.hasElapsed(TimeUnit.SECONDS.toTicks()));
    Assertions.assertTrue(ticker.hasElapsed(TimeUnit.TICKS.toTicks()));

    Assertions.assertFalse(ticker.hasElapsed(1, TimeUnit.DAYS));
    Assertions.assertFalse(ticker.hasElapsed(1, TimeUnit.HOURS));
    Assertions.assertFalse(ticker.hasElapsed(2, TimeUnit.MINUTES));
    Assertions.assertTrue(ticker.hasElapsed(1, TimeUnit.MINUTES));
    Assertions.assertTrue(ticker.hasElapsed(1, TimeUnit.SECONDS));
    Assertions.assertTrue(ticker.hasElapsed(1, TimeUnit.TICKS));
    Assertions.assertTrue(ticker.hasElapsed(TimeUnit.MINUTES.toTicks(), TimeUnit.TICKS));

    ticker = new TimeTicker(TickDuration.of(TimeUnit.SECONDS, 2));
    ticker.set(1);
    Assertions.assertFalse(ticker.hasElapsed(3, TimeUnit.SECONDS));
    Assertions.assertTrue(ticker.hasElapsed(2, TimeUnit.SECONDS));
    Assertions.assertTrue(ticker.hasElapsed(1, TimeUnit.SECONDS));
    Assertions.assertTrue(ticker.hasElapsed(1, TimeUnit.TICKS));
    ticker.set(3);
    Assertions.assertFalse(ticker.hasElapsed(7, TimeUnit.SECONDS));
    Assertions.assertTrue(ticker.hasElapsed(6, TimeUnit.SECONDS));
    Assertions.assertTrue(ticker.hasElapsed(3, TimeUnit.SECONDS));
    Assertions.assertTrue(ticker.hasElapsed(3, TimeUnit.TICKS));
  }

}

