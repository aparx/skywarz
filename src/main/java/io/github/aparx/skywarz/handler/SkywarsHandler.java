package io.github.aparx.skywarz.handler;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:25
 * @since 1.0
 */
public interface SkywarsHandler {

  @CanIgnoreReturnValue
  boolean load();

  @CanIgnoreReturnValue
  boolean unload();

  boolean isLoaded();

}
