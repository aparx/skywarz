package io.github.aparx.skywarz.game.phase.phases;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.WeakGroupAudience;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.item.items.LeaveItem;
import io.github.aparx.skywarz.game.item.items.waiting.TeamSelectorItem;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.game.phase.features.LevelAnimator;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import io.github.aparx.skywarz.utils.tick.Ticker;
import org.bukkit.GameMode;
import org.bukkit.Sound;
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
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:58
 * @since 1.0
 */
public class WaitingPhase extends GamePhase {

  private final Ticker trigger;

  private boolean lastMinimumPlayers;
  private int lastPlayerSize;

  private long secsLeft;

  public WaitingPhase(@NonNull GamePhaseCycler cycler) {
    super(MatchState.WAITING, cycler,
        TickDuration.of(TimeUnit.SECONDS, 3),
        /* Update all two ticks to save performance */
        TickDuration.of(TimeUnit.TICKS, 2));
    trigger = new TimeTicker(getInterval());
  }

  @Override
  public void handleJoin(SkywarsPlayer player) {
    // Manage entity
    Player entity = player.getOnline();
    Match match = getMatch();
    PlayerSnapshot.ofReset(entity,
        match.getArena().getData().getLobby(),
        GameMode.ADVENTURE).restore(entity);
    entity.getInventory().setItem(LeaveItem.SLOT, Skywars.getInstance()
        .getGameItemManager()
        .getItems()
        .require(LeaveItem.class)
        .create(match, entity));

    Skywars.getInstance()
        .getGameItemManager()
        .getItems()
        .require(TeamSelectorItem.class)
        .give(match, entity);
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop(StopReason reason) {
    super.onStop(reason);
    if (reason == StopReason.TIME)
      findMatch()
          .map(Match::getAudience)
          .ifPresent((x) -> x.playSound(Sound.ENTITY_PLAYER_LEVELUP, .25f, .75f));
  }

  @Override
  protected void updateTick() {
    Ticker ticker = getTicker();
    Match match = getMatch();
    WeakGroupAudience<SkywarsPlayer> players = match.getAudience();
    final int minPlayers = match.getMinPlayerSize();
    final int playerSize = players.size();
    int missingPlayerAmount = minPlayers - playerSize;
    boolean lastMinimumPlayers = this.lastMinimumPlayers;
    boolean hasMinimumPlayers = missingPlayerAmount <= 0;
    this.lastMinimumPlayers = hasMinimumPlayers;
    if (!hasMinimumPlayers) ticker.set(0);
    trigger.tick();
    if (trigger.isCycling(TimeUnit.SECONDS)) {
      int lastPlayerSize = this.lastPlayerSize;
      this.lastPlayerSize = playerSize;
      if (hasMinimumPlayers) {
        long durationSecs = getDuration().toSeconds();
        long elapsed = ticker.getElapsed(TimeUnit.SECONDS);
        secsLeft = durationSecs - elapsed;
        if (((elapsed == 0 || secsLeft <= 3)
            || (secsLeft <= 20 && secsLeft % 5 == 0)
            || (secsLeft <= 60 && secsLeft % 15 == 0)
            || secsLeft % 30 == 0)) {
          Map<String, Long> time = Map.of("time", secsLeft);
          players.forEach((player) -> {
            player.sendFormattedMessage(MessageKeys.Match.BROADCAST_START, time);
            player.playSound(Sound.BLOCK_DISPENSER_DISPENSE, .5f, 1.5f);
          });
        }
      } else if (playerSize != lastPlayerSize || trigger.isCycling(30, TimeUnit.SECONDS))
        players.sendFormattedMessage(MessageKeys.Match.BROADCAST_REQUIRE,
            Map.of("required", minPlayers, "missing", missingPlayerAmount));
    }
    if (hasMinimumPlayers) LevelAnimator.animate(this, (int) secsLeft);
    else if (lastMinimumPlayers) LevelAnimator.animate(this, 0);
  }

  // EVENT HANDLERS

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
