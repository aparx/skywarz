package io.github.aparx.skywarz.game.phase;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.game.match.SkywarsMatch;
import io.github.aparx.skywarz.game.match.SkywarsMatchState;
import io.github.aparx.skywarz.game.phase.phases.done.DonePhase;
import io.github.aparx.skywarz.game.phase.phases.playing.PlayingPhase;
import io.github.aparx.skywarz.game.phase.phases.idle.IdlePhase;
import lombok.Getter;
import lombok.Synchronized;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 02:13
 * @since 1.0
 */
public final class SkywarsPhaseCycler {

  private static final Function<SkywarsPhaseCycler, Set<SkywarsPhase>> DEFAULT_PHASE_FACTORY =
      (cycler) -> Set.of(new IdlePhase(cycler), new PlayingPhase(cycler), new DonePhase(cycler));

  private final @NonNull WeakReference<SkywarsMatch> match;

  private final ImmutableMap<SkywarsMatchState, SkywarsPhase> phaseMap;

  @Getter(onMethod_ = {@Synchronized})
  private volatile SkywarsMatchState state = SkywarsMatchState.values()[0];

  public SkywarsPhaseCycler(@NonNull SkywarsMatch match) {
    this(match, DEFAULT_PHASE_FACTORY);
  }

  public SkywarsPhaseCycler(
      @NonNull SkywarsMatch match,
      @NonNull Function<SkywarsPhaseCycler, @NonNull Set<SkywarsPhase>> factory) {
    Preconditions.checkNotNull(match, "Match must not be null");
    Preconditions.checkNotNull(factory, "Factory must not be null");
    this.match = new WeakReference<>(match);
    Set<SkywarsPhase> phases = factory.apply(this);
    Preconditions.checkNotNull(phases, "Phases must not be null");
    ImmutableMap.Builder<SkywarsMatchState, SkywarsPhase> builder =
        ImmutableMap.builderWithExpectedSize(phases.size());
    for (SkywarsPhase phase : phases)
      builder.put(phase.getState(), phase);
    this.phaseMap = builder.build();
    Preconditions.checkState(phaseMap.size() >= 2,
        "Must at least include two different phases!");
    syncToStateFromMatch();
  }


  @Synchronized
  public Optional<SkywarsPhase> peek() {
    return findPhase(state.next());
  }

  @Synchronized
  @CanIgnoreReturnValue
  public Optional<SkywarsPhase> cycleNext() {
    return cycleJump(state.next());
  }

  @Synchronized
  @CanIgnoreReturnValue
  public Optional<SkywarsPhase> cycleJump(@NonNull SkywarsMatchState state) {
    Preconditions.checkNotNull(state, "State must not be null");
    if (state == this.state)
      return findPhase(state);
    findPhase(this.state).ifPresent((x) -> x.stop(SkywarsPhase.StopReason.UNKNOWN));
    return findMatch().flatMap((match) -> {
      match.setState(state);
      Optional<SkywarsPhase> phase = findPhase(state);
      phase.ifPresent(SkywarsPhase::start);
      this.state = state;
      SkywarsArena source = match.getArena().getSource();
      if (source != null) source.getSignHandler().update();
      return phase;
    });
  }


  @Synchronized
  public Optional<SkywarsPhase> findPhase(SkywarsMatchState state) {
    return Optional.ofNullable(phaseMap.get(state));
  }

  public Optional<SkywarsPhase> getPhase() {
    return findPhase(getState());
  }

  @Synchronized
  public void syncToStateFromMatch() {
    cycleJump(getMatch().getState());
  }

  public @NonNull SkywarsMatch getMatch() {
    SkywarsMatch match = this.match.get();
    Preconditions.checkState(match != null, "Match has become invalid");
    return match;
  }

  public Optional<SkywarsMatch> findMatch() {
    return Optional.ofNullable(this.match.get());
  }

}
