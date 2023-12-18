package io.github.aparx.skywarz.game.phase.phases.done;

import io.github.aparx.skywarz.game.phase.GamePhaseListener;
import io.github.aparx.skywarz.game.phase.phases.LobbyPhaseListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-07 10:42
 * @since 1.0
 */
public class DoneListener extends GamePhaseListener<DonePhase> {

  private final LobbyPhaseListener lobbyPhaseListener;

  public DoneListener(@NonNull DonePhase phase) {
    super(phase);
    lobbyPhaseListener = new LobbyPhaseListener(phase);
  }

  @Override
  public void load() {
    super.load();
    lobbyPhaseListener.load();
  }

  @Override
  public void unload() {
    super.unload();
    lobbyPhaseListener.unload();
  }

  @EventHandler(priority = EventPriority.NORMAL)
  void onRespawn(PlayerRespawnEvent event) {
    // handle late respawns in case a player was not able to respawn in time
    Player entity = event.getPlayer();
    filterMatchFromPlayer(entity).ifPresent((match) -> {
      event.setRespawnLocation(match.getArena().getData().getLobby());
    });
  }

}
