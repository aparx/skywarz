package io.github.aparx.skywarz.game.phase.phases;

import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.arena.GameSettings;
import io.github.aparx.skywarz.game.arena.snapshot.ArenaDataSnapshot;
import io.github.aparx.skywarz.game.arena.snapshot.ArenaSnapshot;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:58
 * @since 1.0
 */
public class PlayingPhase extends GamePhase {

  // TODO move to game settings
  private static final TickDuration PROTECTION_PHASE_TIME = TickDuration.of(TimeUnit.SECONDS, 60);

  public PlayingPhase(@NonNull GamePhaseCycler cycler) {
    super(MatchState.PLAYING, cycler, TickDuration.of(TimeUnit.SECONDS, 2));
  }

  @Override
  public void join(SkywarsPlayer player) {
    PlayerMatchData matchData = player.getMatchData();
    if (isProtectionPhase()) {

    }
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void updateTick() {

  }

  // EVENT HANDLERS

  protected boolean hasProtectionPhase() {
    return findMatch()
        .map(Match::getArena)
        .map(ArenaSnapshot::getData)
        .map(ArenaDataSnapshot::getSettings)
        .map(GameSettings::getFlags)
        .filter(GameSettings.Flags::hasProtectionPhase)
        .isPresent();
  }

  protected boolean isProtectionPhase() {
    return hasProtectionPhase() && getTicker().hasElapsed(PROTECTION_PHASE_TIME);
  }


}
