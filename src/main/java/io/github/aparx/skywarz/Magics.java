package io.github.aparx.skywarz;

import com.google.common.base.Functions;
import com.google.common.base.Suppliers;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 02:33
 * @since 1.0
 */
@UtilityClass
public final class Magics {

  /** Boolean that determines whether a match is actually winnable when one team is left */
  public static final boolean GAME_WINNABLE = !isDevelopment();

  public static final TickDuration DEV_PLAYING_DURATION = TickDuration.of(TimeUnit.MINUTES, 1);

  public static final TickDuration DEV_IDLE_DURATION = TickDuration.of(TimeUnit.SECONDS, 3);

  private static volatile boolean developmentFlag;

  public static boolean isDevelopment() {
    // inner synchronized for better code visibility
    synchronized (Magics.class) {
      return developmentFlag;
    }
  }

  public static void setDevelopment(boolean flag) {
    // inner synchronized for better code visibility
    synchronized (Magics.class) {
      developmentFlag = flag;
    }
  }

}
