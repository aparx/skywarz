package io.github.aparx.skywarz.entity.data.types;

import io.github.aparx.skywarz.entity.data.SkywarsPlayerData;
import io.github.aparx.skywarz.game.match.Match;
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
public final class MainPlayerData extends SkywarsPlayerData {

  private boolean isSpectator;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private WeakReference<Match> currentMatch;

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

}
