package io.github.aparx.skywarz.game.team;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.WeakGroupAudience;
import io.github.aparx.skywarz.game.match.Match;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Iterator;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 04:57
 * @since 1.0
 */
@Getter
public class Team implements Iterable<SkywarsPlayer> {

  private final @NonNull Match match;

  private final @NonNull TeamEnum team;

  private final WeakGroupAudience<SkywarsPlayer> members = new WeakGroupAudience<>();

  public Team(@NonNull Match match, @NonNull TeamEnum team) {
    Preconditions.checkNotNull(match, "Match must not be null");
    Preconditions.checkNotNull(team, "Team must not be null");
    this.match = match;
    this.team = team;
  }

  @Override
  public @NonNull Iterator<SkywarsPlayer> iterator() {
    return members.iterator();
  }
}
