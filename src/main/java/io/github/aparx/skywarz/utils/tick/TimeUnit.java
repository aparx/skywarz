package io.github.aparx.skywarz.utils.tick;

import io.github.aparx.bufig.ArrayPath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:49
 * @since 1.0
 */
@RequiredArgsConstructor
public enum TimeUnit {

  TICKS(1, ArrayPath.of("ticks")),
  SECONDS(20, ArrayPath.of("seconds")),
  MINUTES(60 * SECONDS.ticks, ArrayPath.of("minutes")),
  HOURS(60 * MINUTES.ticks, ArrayPath.of("hours")),
  DAYS(24 * HOURS.ticks, ArrayPath.of("days"));

  private final long ticks;

  @Getter
  private final ArrayPath messageKey;

  public long toTicks() {
    return ticks;
  }

  public long toTicks(long amount) {
    return amount * ticks;
  }

}
