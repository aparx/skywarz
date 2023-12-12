package io.github.aparx.skywarz.events.match.phase;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.events.match.MatchEvent;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.phase.GamePhase;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 06:52
 * @since 1.0
 */
@Getter
public class MatchPhaseEvent extends MatchEvent {

  private final @NonNull GamePhase phase;

  public MatchPhaseEvent(GameMatch match, @NonNull GamePhase phase) {
    super(match);
    Preconditions.checkNotNull(phase, "Phase must not be null");
    this.phase = phase;
  }
}
