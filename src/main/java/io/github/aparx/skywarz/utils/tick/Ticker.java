package io.github.aparx.skywarz.utils.tick;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.function.LongUnaryOperator;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:51
 * @since 1.0
 */
public interface Ticker {

  void reset();

  @CanIgnoreReturnValue
  long tick();

  void set(long ticks);

  @CanIgnoreReturnValue
  long update(LongUnaryOperator updater);

  long getElapsed();

  long getElapsed(TimeUnit time);

  boolean hasElapsed(long inclusiveAmount);

  boolean hasElapsed(long inclusiveAmount, TimeUnit time);

  default boolean hasElapsed(TickDuration duration) {
    return hasElapsed(duration.getAmount(), duration.getUnit());
  }

  default boolean isCycling(long ticks) {
    return getElapsed() % ticks == 0;
  }

  default boolean isCycling(long ticks, TimeUnit time) {
    return getElapsed(TimeUnit.TICKS) % time.toTicks(ticks) == 0;
  }

  default boolean isCycling(TimeUnit time) {
    return isCycling(1, time);
  }

}
