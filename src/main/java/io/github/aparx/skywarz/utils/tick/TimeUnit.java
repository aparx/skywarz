package io.github.aparx.skywarz.utils.tick;

import io.github.aparx.bufig.ArrayPath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:49
 * @since 1.0
 */
@RequiredArgsConstructor
public enum TimeUnit {

  TICKS(1),
  SECONDS(20),
  MINUTES(60 * SECONDS.ticks),
  HOURS(60 * MINUTES.ticks),
  DAYS(24 * HOURS.ticks);

  private final long ticks;

  public long toTicks() {
    return ticks;
  }

  public long toTicks(long amount) {
    return amount * ticks;
  }

}
