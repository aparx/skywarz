package io.github.aparx.skywarz.game.phase.phases;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:58
 * @since 1.0
 */
public class DonePhase extends GamePhase {

  public DonePhase(@NonNull GamePhaseCycler cycler) {
    super(MatchState.DONE, cycler, TickDuration.of(TimeUnit.TICKS, 1));
  }

  @Override
  public void join(SkywarsPlayer player) {}


  @Override
  protected void onStop(StopReason reason) {
    // Remove from manager
    findMatch().ifPresent((match) -> Skywars.getInstance().getMatchManager().remove(match));
  }

  @Override
  protected void updateTick() {
    Bukkit.broadcastMessage("done");
  }
}
