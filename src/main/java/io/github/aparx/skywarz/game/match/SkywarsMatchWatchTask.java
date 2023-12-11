package io.github.aparx.skywarz.game.match;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.WeakPlayerGroup;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import io.github.aparx.skywarz.utils.tick.Ticker;
import lombok.NonNull;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.lang.ref.WeakReference;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 10:50
 * @since 1.0
 */
public final class SkywarsMatchWatchTask {

  private BukkitTask task;

  private final TickDuration interval = TickDuration.of(TimeUnit.SECONDS, 5);

  private final WeakReference<SkywarsMatch> match;

  private final Ticker playerlessTicker = new TimeTicker(interval);

  public SkywarsMatchWatchTask(@NonNull SkywarsMatch match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    this.match = new WeakReference<>(match);
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean start() {
    if (task != null) return false;
    TickDuration minPlayerlessTime = TickDuration.of(TimeUnit.SECONDS, 15);
    task = Bukkit.getScheduler().runTaskTimer(Skywars.plugin(), () -> {
      tick(minPlayerlessTime);
    }, 0, interval.toTicks());
    return true;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean stop() {
    if (task == null) return false;
    task.cancel();
    task = null;
    return true;
  }

  public @NonNull SkywarsMatch getMatch() {
    SkywarsMatch match = this.match.get();
    Preconditions.checkState(match != null, "Match has become invalid");
    return match;
  }

  public void tick(@NonNull TickDuration minPlayerlessTimeForRemoval) {
    try {
      SkywarsMatch match = getMatch();
      WeakPlayerGroup audience = match.getAudience();
      if (audience.online().findAny().isEmpty()) {
        playerlessTicker.tick();
        // after 15 seconds close the match
        if (playerlessTicker.hasElapsed(minPlayerlessTimeForRemoval)
            && Skywars.getInstance().getMatchManager().remove(match))
          Skywars.logger().log(Level.INFO, "Match {0} closed due to inactivity", match.getId());
      } else playerlessTicker.reset();
    } catch (Exception e) {
      stop();
      throw new RuntimeException(e);
    }
  }

}
