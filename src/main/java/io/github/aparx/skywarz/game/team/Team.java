package io.github.aparx.skywarz.game.team;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.WeakPlayerGroup;
import io.github.aparx.skywarz.game.match.Match;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 04:57
 * @since 1.0
 */
@Getter
public class Team extends WeakPlayerGroup {

  private final @NonNull WeakReference<Match> match;

  private final @NonNull TeamEnum teamEnum;

  public Team(@NonNull Match match, @NonNull TeamEnum teamEnum) {
    Preconditions.checkNotNull(match, "Match must not be null");
    Preconditions.checkNotNull(teamEnum, "TeamEnum must not be null");
    this.match = new WeakReference<>(match);
    this.teamEnum = teamEnum;
  }

  public @NonNull Match getMatch() {
    Match match = this.match.get();
    Preconditions.checkState(match != null, "Match has become invalid");
    return match;
  }

  /** Returns true if players can still join this team. */
  public boolean hasSpace() {
    return size() < getMatch().getArena().getData().getSettings().getTeamSize();
  }

  @Override
  public boolean add(SkywarsPlayer member) {
    Preconditions.checkNotNull(member, "Member must not be null");
    Preconditions.checkState(hasSpace(), "No space for new members anymore");
    if (!super.add(member)) return false;
    member.getMatchData().setTeam(this);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    if (!super.remove(o)) return false;
    ((SkywarsPlayer) o).getMatchData().setTeam(null);
    return true;
  }
}
