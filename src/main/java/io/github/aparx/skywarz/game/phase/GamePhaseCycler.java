package io.github.aparx.skywarz.game.phase;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.phase.phases.DonePhase;
import io.github.aparx.skywarz.game.phase.phases.PlayingPhase;
import io.github.aparx.skywarz.game.phase.phases.WaitingPhase;
import lombok.Getter;
import lombok.Synchronized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 02:13
 * @since 1.0
 */
public final class GamePhaseCycler {

  private static final Function<GamePhaseCycler, Set<GamePhase>> DEFAULT_PHASE_FACTORY =
      (cycler) -> Set.of(new WaitingPhase(cycler), new PlayingPhase(cycler), new DonePhase(cycler));

  private final @NonNull WeakReference<Match> match;

  private final ImmutableMap<MatchState, GamePhase> phaseMap;

  @Getter(onMethod_ = {@Synchronized})
  private volatile MatchState state = MatchState.values()[0];

  public GamePhaseCycler(@NonNull Match match) {
    this(match, DEFAULT_PHASE_FACTORY);
  }

  public GamePhaseCycler(
      @NonNull Match match,
      @NonNull Function<GamePhaseCycler, @NonNull Set<GamePhase>> factory) {
    Preconditions.checkNotNull(match, "Match must not be null");
    Preconditions.checkNotNull(factory, "Factory must not be null");
    this.match = new WeakReference<>(match);
    Set<GamePhase> phases = factory.apply(this);
    Preconditions.checkNotNull(phases, "Phases must not be null");
    ImmutableMap.Builder<MatchState, GamePhase> builder =
        ImmutableMap.builderWithExpectedSize(phases.size());
    for (GamePhase phase : phases)
      builder.put(phase.getState(), phase);
    this.phaseMap = builder.build();
    Preconditions.checkState(phaseMap.size() >= 2,
        "Must at least include two different phases!");
    syncToStateFromMatch();
  }


  @Synchronized
  public Optional<GamePhase> peek() {
    return findPhase(state.next());
  }

  @Synchronized
  @CanIgnoreReturnValue
  public Optional<GamePhase> cycleNext() {
    return cycleJump(state.next());
  }

  @Synchronized
  @CanIgnoreReturnValue
  public Optional<GamePhase> cycleJump(@NonNull MatchState state) {
    Preconditions.checkNotNull(state, "State must not be null");
    if (state == this.state)
      return findPhase(state);
    findPhase(this.state).ifPresent((x) -> x.stop(GamePhase.StopReason.MANUALLY));
    getMatch().setState(state);
    this.state = state;
    return findPhase(state).map((nextPhase) -> {
      nextPhase.start();
      return nextPhase;
    });
  }

  @Synchronized
  public Optional<GamePhase> findPhase(MatchState state) {
    return Optional.ofNullable(phaseMap.get(state));
  }

  public Optional<GamePhase> getPhase() {
    return findPhase(getState());
  }

  @Synchronized
  public void syncToStateFromMatch() {
    cycleJump(getMatch().getState());
  }

  public @NonNull Match getMatch() {
    Match match = this.match.get();
    Preconditions.checkState(match != null, "Match has become invalid");
    return match;
  }

  public Optional<Match> findMatch() {
    return Optional.ofNullable(this.match.get());
  }

}
