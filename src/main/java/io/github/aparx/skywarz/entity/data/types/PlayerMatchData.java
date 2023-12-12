package io.github.aparx.skywarz.entity.data.types;

import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.SkywarsPlayerData;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsAccumulator;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.kit.GameKit;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.team.GameTeam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 05:19
 * @since 1.0
 */
@Getter
@Setter
public final class PlayerMatchData extends SkywarsPlayerData {

  private final UUID uuid;

  private boolean isSpectator;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private WeakReference<GameMatch> currentMatch;

  private WeakReference<GameTeam> team;

  private PlayerSnapshot snapshot;

  private GameKit kit;

  /** This statistic is independent of the player's main statistics and only focuses on one match */
  private @NonNull PlayerStatsAccumulator statistics;

  public PlayerMatchData(@NonNull SkywarsPlayer player) {
    this.uuid = player.getId();
    this.statistics = new PlayerStatsAccumulator(player.getId());
  }

  public @Nullable GameMatch getMatch() {
    if (currentMatch == null)
      return null;
    return currentMatch.get();
  }

  public void setMatch(@Nullable GameMatch match) {
    currentMatch = new WeakReference<>(match);
  }

  public boolean isInMatch() {
    return currentMatch != null && currentMatch.get() != null;
  }

  public @Nullable GameTeam getTeam() {
    if (team == null)
      return null;
    return team.get();
  }

  public void setTeam(GameTeam team) {
    this.team = new WeakReference<>(team);
  }

  public boolean isInTeam() {
    return team != null && team.get() != null;
  }

  public boolean hasKit() {
    return kit != null;
  }


}
