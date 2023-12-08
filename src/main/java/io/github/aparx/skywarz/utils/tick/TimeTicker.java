package io.github.aparx.skywarz.utils.tick;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:53
 * @since 1.0
 */
@Getter
public class TimeTicker implements Ticker {

  @Getter(AccessLevel.NONE)
  protected final @NonNull AtomicLong ticks;

  private final @NonNull TickDuration interval;

  public TimeTicker() {
    this(TickDuration.ofTick());
  }

  public TimeTicker(@NonNull TimeUnit unit) {
    this(TickDuration.of(unit, 1));
  }

  public TimeTicker(@NonNull TickDuration interval) {
    Preconditions.checkNotNull(interval, "interval must not be null");
    this.interval = interval;
    this.ticks = new AtomicLong();
  }

  @Override
  public void reset() {
    ticks.set(0);
  }

  @Override
  public long tick() {
    return ticks.incrementAndGet();
  }

  @Override
  public void set(long ticks) {
    this.ticks.set(ticks);
  }

  public void set(TickDuration duration) {
    this.ticks.set(duration.getAmount(getInterval().getUnit()) / getInterval().getAmount());
  }

  @Override
  @CanIgnoreReturnValue
  public long update(LongUnaryOperator updater) {
    return this.ticks.updateAndGet(updater);
  }

  @Override
  public long getElapsed() {
    return ticks.get();
  }

  @Override
  public long getElapsed(TimeUnit time) {
    return TickDuration.of(interval.getUnit(),
            ticks.get() * Math.max(interval.getAmount(), 1))
        .getAmount(time);
  }

  public TickDuration toElapsedDuration() {
    return TickDuration.of(TimeUnit.TICKS, getElapsed(TimeUnit.TICKS));
  }

  @Override
  public boolean hasElapsed(long inclusiveAmount) {
    return getElapsed() >= inclusiveAmount;
  }

  @Override
  public boolean hasElapsed(long inclusiveAmount, TimeUnit time) {
    return getElapsed(TimeUnit.TICKS) >= time.toTicks(inclusiveAmount);
  }
}
