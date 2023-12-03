package io.github.aparx.skywarz.handler;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import lombok.Getter;
import lombok.Synchronized;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:26
 * @since 1.0
 */
public abstract class LockingSkywarsHandler implements SkywarsHandler {

  protected final transient Object handlerLock = new Object();

  @Getter(onMethod_ = {@Synchronized("handlerLock")})
  private volatile boolean isLoaded;

  @Override
  @Synchronized("handlerLock")
  @CanIgnoreReturnValue
  public final boolean load() {
    if (isLoaded()) return false;
    try {
      onLoad();
      isLoaded = true;
    } catch (Exception e) {
      Skywars.logger().severe("Error on handler load");
      throw e; // pass to parent to cause shutdown if needed
    }
    return true;
  }

  @Override
  @Synchronized("handlerLock")
  @CanIgnoreReturnValue
  public final boolean unload() {
    if (!isLoaded()) return false;
    isLoaded = false;
    onUnload();
    return true;
  }

  protected void onLoad() {}

  protected void onUnload() {}

}
