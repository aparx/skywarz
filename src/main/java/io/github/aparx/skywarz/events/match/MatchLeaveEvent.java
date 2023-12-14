package io.github.aparx.skywarz.events.match;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.game.match.GameMatch;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 06:37
 * @since 1.0
 */
@Getter
@Setter
public class MatchLeaveEvent extends MatchEvent {

  private final @NonNull SkywarsPlayer player;

  public MatchLeaveEvent(@NonNull GameMatch match, @NonNull SkywarsPlayer player) {
    super(match);
    Preconditions.checkNotNull(player, "Player must not be null");
    this.player = player;
  }

}
