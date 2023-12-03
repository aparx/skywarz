package io.github.aparx.skywarz.game.match;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 06:18
 * @since 1.0
 */
public enum MatchState {

  LOBBY,
  PLAYING,
  DONE;

  public boolean isJoinable() {
    return this != DONE;
  }

}
