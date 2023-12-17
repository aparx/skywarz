package io.github.aparx.skywarz.game.match;

import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 06:18
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum GameMatchState {

  /** Match is not joinable since it is still in setup */
  SETUP(null),
  /** Players wait for others to join */
  IDLE(TickDuration.of(TimeUnit.SECONDS, 45)),
  /** Players are in the midst of playing */
  PLAYING(TickDuration.of(TimeUnit.MINUTES, 25)),
  /** Match is done and players are sent back to lobby to celebrate the winner */
  DONE(TickDuration.of(TimeUnit.SECONDS, 15));

  private final @Nullable TickDuration defaultDuration;

  public String getTranslatedName() {
    return Language.getInstance().get(MessageKeys.getMatchStateKey(this)).get();
  }

  public boolean isJoinable() {
    return this != SETUP && this != DONE;
  }

  public GameMatchState previous() {
    int index = ordinal() - 1;
    GameMatchState[] values = values();
    return index < 0 ? values[values.length - 1] : values[index];
  }

  public GameMatchState next() {
    GameMatchState[] states = values();
    return states[(1 + ordinal()) % states.length];
  }

  public boolean isBeforeOrEqual(GameMatchState state) {
    return ordinal() <= state.ordinal();
  }

  public boolean isAfterOrEqual(GameMatchState state) {
    return ordinal() >= state.ordinal();
  }
}
