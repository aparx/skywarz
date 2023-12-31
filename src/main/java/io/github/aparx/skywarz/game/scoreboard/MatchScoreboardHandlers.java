package io.github.aparx.skywarz.game.scoreboard;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.match.GameMatch;
import lombok.AccessLevel;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-08 12:47
 * @since 1.0
 */
@Getter
public final class MatchScoreboardHandlers {

  @Getter(AccessLevel.NONE)
  private final @NonNull WeakReference<GameMatch> match;

  private final EnumMap<MatchScoreboard, MatchScoreboardHandler> handlers =
      new EnumMap<>(MatchScoreboard.class);

  public MatchScoreboardHandlers(@NonNull GameMatch match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    this.match = new WeakReference<>(match);
  }

  public @NonNull MatchScoreboardHandler getHandler(@NonNull MatchScoreboard scoreboard) {
    Preconditions.checkNotNull(scoreboard, "Scoreboard must not be null");
    return handlers.computeIfAbsent(scoreboard, (sb) -> new MatchScoreboardHandler(sb, getMatch()));
  }

  public void clear() {
    handlers.clear();
  }

  public Optional<GameMatch> findMatch() {
    return Optional.ofNullable(match.get());
  }

  public GameMatch getMatch() {
    return findMatch().orElseThrow();
  }

}
