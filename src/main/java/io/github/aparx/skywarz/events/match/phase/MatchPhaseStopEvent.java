package io.github.aparx.skywarz.events.match.phase;

import io.github.aparx.skywarz.game.match.SkywarsMatch;
import io.github.aparx.skywarz.game.phase.SkywarsPhase;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 06:51
 * @since 1.0
 */
@Getter
public class MatchPhaseStopEvent extends MatchPhaseEvent {

  private final SkywarsPhase.@NonNull StopReason reason;

  public MatchPhaseStopEvent(
      @Nullable SkywarsMatch match,
      SkywarsPhase.@NonNull StopReason reason,
      @NonNull SkywarsPhase phase) {
    super(match, phase);
    this.reason = reason;
  }

  public boolean isMatchInvalid() {
    return getMatch() == null;
  }

  @Override
  public @Nullable SkywarsMatch getMatch() {
    return super.getMatch();
  }
}
