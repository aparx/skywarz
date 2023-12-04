package io.github.aparx.skywarz.entity.data.types;

import io.github.aparx.skywarz.entity.data.SkywarsPlayerData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.team.Team;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 05:19
 * @since 1.0
 */
@Getter
@Setter
public final class PlayerMatchData extends SkywarsPlayerData {

  private boolean isSpectator;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private WeakReference<Match> currentMatch;

  private WeakReference<Team> team;

  private PlayerSnapshot snapshot;

  public @Nullable Match getMatch() {
    if (currentMatch == null)
      return null;
    return currentMatch.get();
  }

  public void setMatch(@Nullable Match match) {
    currentMatch = new WeakReference<>(match);
  }

  public boolean isInMatch() {
    return currentMatch != null && currentMatch.get() != null;
  }

  public @Nullable Team getTeam() {
    if (team == null)
      return null;
    return team.get();
  }

  public void setTeam(Team team) {
    this.team = new WeakReference<>(team);
  }

  public boolean isInTeam() {
    return team != null && team.get() != null;
  }

}
