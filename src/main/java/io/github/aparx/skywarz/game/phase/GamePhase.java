package io.github.aparx.skywarz.game.phase;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.scoreboard.MatchScoreboard;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:45
 * @since 1.0
 */
@Getter
public abstract class GamePhase implements Listener {

  private final @NonNull TickDuration interval;

  private final @NonNull GamePhaseCycler cycler;

  private final @NonNull TickDuration duration;

  private final @NonNull MatchState state;

  @Getter(AccessLevel.NONE)
  private volatile BukkitTask task;

  private final @NonNull TimeTicker ticker;

  @Setter(AccessLevel.PROTECTED)
  private @Nullable GamePhaseListener<?> listener;

  public GamePhase(@NonNull MatchState state,
                   @NonNull GamePhaseCycler cycler,
                   @NonNull TickDuration duration) {
    this(state, cycler, duration, TickDuration.ofTick());
  }

  public GamePhase(
      @NonNull MatchState state,
      @NonNull GamePhaseCycler cycler,
      @NonNull TickDuration duration,
      @NonNull TickDuration interval) {
    Preconditions.checkNotNull(state, "State must not be null");
    Preconditions.checkNotNull(cycler, "Cycler must not be null");
    Preconditions.checkNotNull(interval, "Interval must not be null");
    Preconditions.checkNotNull(duration, "Duration must not be null");
    this.state = state;
    this.cycler = cycler;
    this.interval = interval;
    this.duration = duration;
    this.ticker = new TimeTicker(interval);
  }

  protected abstract void updateTick();

  /** Event method called when a player joins while this phase is ongoing. */
  public abstract void handleJoin(SkywarsPlayer player);

  /** Event method called when a player leaves while this phase is ongoing. */
  public void handleLeave(SkywarsPlayer player) {}

  protected void onStart() {
    if (listener != null) listener.load();
  }

  protected void onStop(StopReason reason) {
    if (listener != null) listener.unload();
  }

  @Synchronized
  @CanIgnoreReturnValue
  public final boolean start() {
    if (task != null) return false;
    onStart();
    ticker.reset();
    task = Bukkit.getScheduler().runTaskTimer(Skywars.plugin(), this::tick, 0L, interval.toTicks());
    return true;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public final boolean stop(StopReason reason) {
    if (task == null) return false;
    try {
      onStop(reason);
      return true;
    } finally {
      if (task != null) task.cancel();
      task = null;
    }
  }

  @Synchronized
  public final void tick() {
    try {
      if (findMatch().isEmpty()) {
        Skywars.logger().fine("[GamePhaseCycler] Match became invalid (enforce stop)");
        stop(StopReason.UNKNOWN);
        return;
      }
      if (ticker.hasElapsed(duration)) {
        stop(StopReason.TIME);
        getCycler().cycleNext();
      } else {
        updateTick();
        ticker.tick();
      }
    } catch (Exception e) {
      Skywars.logger().log(Level.WARNING, "[GamePhaseCycler] Error in game phase", e);
      stop(StopReason.ERROR);
      getCycler().cycleNext();
    }
  }

  public @NonNull Match getMatch() {
    return cycler.getMatch();
  }

  public Optional<Match> findMatch() {
    return cycler.findMatch();
  }

  public enum StopReason {
    /** Phase has been stopped manually for a for this phase possibly unknown reason. */
    UNKNOWN,
    /** Phase has been stopped manually due to an error. */
    ERROR,
    /** Phase has reached end of its duration, thus stopped due to time */
    TIME
  }
}
