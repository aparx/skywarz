package io.github.aparx.skywarz.game.scoreboard;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.game.match.SkywarsMatch;
import lombok.AccessLevel;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-08 12:23
 * @since 1.0
 */
@Getter
public final class MatchScoreboardHandler {

  private final @NonNull MatchScoreboard scoreboard;

  @Getter(AccessLevel.NONE)
  private final @NonNull WeakReference<SkywarsMatch> match;

  private final WeakHashMap<SkywarsPlayer, SpecialScoreboard> scoreboardMap = new WeakHashMap<>();

  public MatchScoreboardHandler(@NonNull MatchScoreboard scoreboard, @NonNull SkywarsMatch match) {
    Preconditions.checkNotNull(scoreboard, "Scoreboard must not be null");
    Preconditions.checkNotNull(match, "Match must not be null");
    this.scoreboard = scoreboard;
    this.match = new WeakReference<>(match);
  }

  public SpecialScoreboard getOrCreateMainScoreboard() {
    return getOrCreateScoreboard(null);
  }

  public SpecialScoreboard getOrCreateScoreboard(@Nullable SkywarsPlayer player) {
    return scoreboardMap.computeIfAbsent(player, (k) ->
        scoreboard.getScoreboard().createScoreboard(getMatch(), k != null ? k.getOnline() : null));
  }

  public Optional<SpecialScoreboard> findScoreboard(@Nullable SkywarsPlayer player) {
    return Optional.ofNullable(scoreboardMap.get(player));
  }

  public Optional<SkywarsMatch> findMatch() {
    return Optional.ofNullable(match.get());
  }

  public SkywarsMatch getMatch() {
    return findMatch().orElseThrow();
  }

}
