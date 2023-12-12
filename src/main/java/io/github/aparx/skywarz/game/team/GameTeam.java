package io.github.aparx.skywarz.game.team;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.entity.GamePlayer;
import io.github.aparx.skywarz.entity.WeakPlayerGroup;
import io.github.aparx.skywarz.game.match.GameMatch;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 04:57
 * @since 1.0
 */
@Getter
public class GameTeam extends WeakPlayerGroup {

  private final @NonNull WeakReference<GameMatch> match;

  private final @NonNull TeamEnum teamEnum;

  public GameTeam(@NonNull GameMatch match, @NonNull TeamEnum teamEnum) {
    Preconditions.checkNotNull(match, "Match must not be null");
    Preconditions.checkNotNull(teamEnum, "TeamEnum must not be null");
    this.match = new WeakReference<>(match);
    this.teamEnum = teamEnum;
  }

  public @NonNull GameMatch getMatch() {
    GameMatch match = this.match.get();
    Preconditions.checkState(match != null, "Match has become invalid");
    return match;
  }

  /** Returns true if players can still join this team. */
  public boolean hasSpace() {
    return size() < getMatch().getTeamSize();
  }

  @Override
  public boolean add(GamePlayer member) {
    Preconditions.checkNotNull(member, "Member must not be null");
    Preconditions.checkState(hasSpace(), "No space for new members anymore");
    if (!super.add(member)) return false;
    member.getMatchData().setTeam(this);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    if (!super.remove(o)) return false;
    ((GamePlayer) o).getMatchData().setTeam(null);
    return true;
  }
}
