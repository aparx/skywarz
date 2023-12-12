package io.github.aparx.skywarz.events.match.phase;

import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.phase.GamePhase;
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

  private final GamePhase.@NonNull StopReason reason;

  public MatchPhaseStopEvent(
      @Nullable GameMatch match,
      GamePhase.@NonNull StopReason reason,
      @NonNull GamePhase phase) {
    super(match, phase);
    this.reason = reason;
  }

  public boolean isMatchInvalid() {
    return getMatch() == null;
  }

  @Override
  public @Nullable GameMatch getMatch() {
    return super.getMatch();
  }
}
