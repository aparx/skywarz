package io.github.aparx.skywarz.events.match;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.entity.GamePlayer;
import io.github.aparx.skywarz.game.match.GameMatch;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 06:37
 * @since 1.0
 */
@Getter
@Setter
public class MatchJoinEvent extends MatchEvent implements Cancellable {

  private final @NonNull Player entity;

  private boolean cancelled;

  public MatchJoinEvent(@NonNull GameMatch match, @NonNull Player entity) {
    super(match);
    Preconditions.checkNotNull(entity, "Entity must not be null");
    this.entity = entity;
  }

  public @NonNull GamePlayer getPlayer() {
    return GamePlayer.getPlayer(getEntity());
  }

}
