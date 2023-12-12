package io.github.aparx.skywarz.events.match;

import io.github.aparx.skywarz.game.match.GameMatch;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Event called when a player joins and arena, but no {@code Match} has been associated to that
 * arena yet. Event is cancellable, which leads to an error thrown at the player trying to join
 * when cancelled.
 *
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 06:34
 * @since 1.0
 */
@Getter
@Setter
public class MatchCreateEvent extends MatchEvent implements Cancellable {

  private boolean cancelled;

  public MatchCreateEvent(@NonNull GameMatch match) {
    super(match);
  }

}
