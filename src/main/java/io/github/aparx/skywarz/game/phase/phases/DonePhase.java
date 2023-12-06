package io.github.aparx.skywarz.game.phase.phases;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.item.GameItem;
import io.github.aparx.skywarz.game.item.items.LeaveItem;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.game.phase.features.LevelAnimator;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:58
 * @since 1.0
 */
public class DonePhase extends GamePhase {

  public DonePhase(@NonNull GamePhaseCycler cycler) {
    super(MatchState.DONE, cycler,
        TickDuration.of(TimeUnit.SECONDS, 15),
        TickDuration.of(TimeUnit.TICKS, 2));
  }

  @Override
  public void handleJoin(SkywarsPlayer player) {
    throw new UnsupportedOperationException();
  }


  @Override
  protected void onStart() {
    super.onStart();
    Match match = getMatch();
    match.getAudience().stream()
        .map(SkywarsPlayer::findOnline)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach((player) -> {
          PlayerSnapshot.ofReset(player, GameMode.ADVENTURE).restore(player);
          player.teleport(match.getArena().getData().getLobby());
          player.getInventory().setItem(LeaveItem.SLOT,
              Skywars.getInstance()
                  .getGameItemManager()
                  .getItems()
                  .require(LeaveItem.class)
                  .create(match, player));
        });
  }

  @Override
  protected void onStop(StopReason reason) {
    // Remove from manager
    findMatch().ifPresent((match) -> Skywars.getInstance().getMatchManager().remove(match));
    // TODO MAP RESET
  }

  @Override
  protected void updateTick() {
    Match match = getMatch();
    long secsLeft = getDuration().toSeconds() - getTicker().getElapsed(TimeUnit.SECONDS);
    if (getTicker().isCycling(TimeUnit.SECONDS)) {
      if (secsLeft % 5 == 0 || secsLeft <= 3)
        match.getAudience().sendFormattedMessage(
            MessageKeys.Match.BROADCAST_CLOSING,
            Map.of("time", secsLeft));
    }
    LevelAnimator.animate(this, (int) secsLeft);
    if (match.getAudience().isEmpty())
      getCycler().cycleNext();
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onRespawn(PlayerRespawnEvent event) {
    // handle late respawns in case a player was not able to respawn in time
    Player entity = event.getPlayer();
    filterMatchFromPlayer(entity).ifPresent((match) -> {
      event.setRespawnLocation(match.getArena().getData().getLobby());
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onPassiveDamage(EntityDamageEvent event) {
    Entity damagee = event.getEntity();
    if (event.isCancelled() || !(damagee instanceof Player)) return;
    event.setCancelled(filterMatchFromPlayer((Player) damagee).isPresent());
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onActiveDamage(EntityDamageByEntityEvent event) {
    Entity damager = event.getDamager();
    if (event.isCancelled() || !(damager instanceof Player)) return;
    event.setCancelled(filterMatchFromPlayer((Player) damager).isPresent());
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onHunger(FoodLevelChangeEvent event) {
    HumanEntity entity = event.getEntity();
    if (event.isCancelled() || !(entity instanceof Player)) return;
    event.setCancelled(filterMatchFromPlayer((Player) entity).isPresent());
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onBreak(BlockBreakEvent event) {
    if (event.isCancelled()) return;
    event.setCancelled(filterMatchFromPlayer(event.getPlayer()).isPresent());
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onPlace(BlockPlaceEvent event) {
    if (event.isCancelled()) return;
    event.setCancelled(filterMatchFromPlayer(event.getPlayer()).isPresent());
  }

}
