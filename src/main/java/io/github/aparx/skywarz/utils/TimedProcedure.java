package io.github.aparx.skywarz.utils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;

import java.util.function.Consumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 21:18
 * @since 1.0
 */
@Getter
public final class TimedProcedure {

  private long nanosPassed;

  @CanIgnoreReturnValue
  public static long executeProcedure(Consumer<TimedProcedure> action) {
    return new TimedProcedure().execute(action);
  }

  @CanIgnoreReturnValue
  public long execute(Consumer<TimedProcedure> action) {
    nanosPassed = System.nanoTime();
    action.accept(this);
    return getElapsedNanos();
  }

  public String toPerformanceString() {
    return String.format("%.3fms", getElapsedMillis());
  }

  public double getElapsedMillis() {
    return getElapsedNanos() / 1E6;
  }

  public long getElapsedNanos() {
    return System.nanoTime() - nanosPassed;
  }

}
