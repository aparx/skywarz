package io.github.aparx.skywarz.events.match.phase;

import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.phase.GamePhase;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 06:51
 * @since 1.0
 */
@Getter
public class MatchPhaseStartEvent extends MatchPhaseEvent {

  public MatchPhaseStartEvent(@NonNull GameMatch match, @NonNull GamePhase phase) {
    super(match, phase);
  }
}
