package io.github.aparx.skywarz.events.match;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.entity.GamePlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerStatsAccumulator;
import io.github.aparx.skywarz.game.match.GameMatch;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Signed;

/**
 * Event called by a {@code Match} when match specific stats of a player are added onto the stats
 * fetched by the database. This event has no knowledge of stats outside the actual stats
 * accumulated during a match.
 *
 * @author aparx (Vinzent Z.)
 * @version 2023-12-10 13:28
 * @since 1.0
 */
@Getter
public class MatchPointsCalculateEvent extends MatchEvent {

  private final @NonNull GamePlayer player;
  private final @NonNull PlayerStatsAccumulator matchStats;

  /**
   * The calculated points to add to the player's current total points.
   * <p>This number may be negative, but the number supplied to the database may always be
   * positive, thus if this value is negative, it is wrapped to zero implicitly.
   */
  @Setter
  private @Signed int points;

  public MatchPointsCalculateEvent(
      @NonNull GameMatch match,
      @NonNull GamePlayer player,
      @NonNull PlayerStatsAccumulator matchStats) {
    super(match);
    Preconditions.checkNotNull(match, "Match must not be null");
    Preconditions.checkNotNull(player, "Player must not be null");
    Preconditions.checkNotNull(matchStats, "Stats must not be null");
    this.player = player;
    this.matchStats = matchStats;
  }

}
