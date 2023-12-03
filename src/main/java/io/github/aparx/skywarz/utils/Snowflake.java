package io.github.aparx.skywarz.utils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Deterministic;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 04:37
 * @since 1.0
 */
@CanIgnoreReturnValue
public interface Snowflake<T> {

  @Deterministic
  @NonNull T getId();

}
