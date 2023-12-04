package io.github.aparx.skywarz.game.match;

import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 06:18
 * @since 1.0
 */
public enum MatchState {

  /** Match is not joinable since it is either resetting or still in setup */
  SETUP,
  /** Players wait for others to join */
  WAITING,
  /** Players are in the midst of playing */
  PLAYING,
  /** Match is done and players are sent back to lobby to celebrate the winner */
  DONE;

  public boolean isJoinable() {
    return this != SETUP;
  }

  public MatchState previous() {
    int index = ordinal() - 1;
    MatchState[] values = values();
    return index < 0 ? values[values.length - 1] : values[index];
  }

  public MatchState next() {
    MatchState[] states = values();
    return states[(1 + ordinal()) % states.length];
  }

}
