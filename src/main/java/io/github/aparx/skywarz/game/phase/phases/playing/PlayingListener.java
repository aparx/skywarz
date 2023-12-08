package io.github.aparx.skywarz.game.phase.phases.playing;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.game.arena.ArenaBox;
import io.github.aparx.skywarz.game.chest.ChestHandler;
import io.github.aparx.skywarz.game.phase.GamePhaseListener;
import io.github.aparx.skywarz.game.phase.features.SkywarsSpectator;
import io.github.aparx.skywarz.game.scoreboard.MatchScoreboard;
import io.github.aparx.skywarz.game.team.Team;
import io.github.aparx.skywarz.language.LazyVariableLookup;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.language.ValueMapPopulators;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-07 10:43
 * @since 1.0
 */
public class PlayingListener extends GamePhaseListener<PlayingPhase> {

  public PlayingListener(@NonNull PlayingPhase phase) {
    super(phase);
  }

  // Chest fill
  @EventHandler(priority = EventPriority.HIGHEST)
  void onBlockExplode(PlayerInteractEvent event) {
    Block clickedBlock = event.getClickedBlock();
    if (clickedBlock == null) return;
    BlockState state = clickedBlock.getState();
    if (!(state instanceof Chest)) return;
    Chest chest = (Chest) state;
    filterMatchFromPlayer(event.getPlayer()).ifPresent((match) -> {
      ChestHandler chestHandler = match.getChestHandler();
      chestHandler.fill(chest.getLocation(), chest.getInventory());
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onMove(PlayerMoveEvent event) {
    if (event.isCancelled()) return;
    Location target = event.getTo();
    if (target == null) return;
    Location origin = event.getFrom();
    if ((target.getBlockX() != origin.getBlockX()
        || target.getBlockZ() != origin.getBlockZ())
        && Objects.equals(target.getWorld(), origin.getWorld()))
      filterMatchFromPlayer(event.getPlayer()).ifPresentOrElse((match) -> {
        Arena source = match.getArena().getSource();
        if (source == null) return;
        ArenaBox box = source.getData().getBox();
        if (SkywarsPlayer.getPlayer(event.getPlayer()).getMatchData().isSpectator()) {
          // disallow spectators that once entered the arena to not leave the arena
          // we only disallow spectators that have been in already, for when the spectator spawn
          // lays outside the arena (which usually is not the case)
          if (!box.isWithinHorizontally(target) && box.isWithinHorizontally(origin))
            event.setTo(event.getFrom());
        } else if (!box.isWithin(target))
          event.setTo(event.getFrom());
      }, () -> {
        // disallow players that do not participate in the match to enter the arena
        getPhase().findMatch().ifPresent((match) -> {
          ArenaBox box = match.getArena().getData().getBox();
          if (box.isWithin(target) && !box.isWithin(origin))
            event.setTo(event.getFrom());
        });
      });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    filterMatchFromPlayer(player).ifPresent((match) -> {
      // TODO maybe move this to the MatchListener?
      PlayerMatchData data = SkywarsPlayer.getPlayer(player).getMatchData();
      event.setCancelled(data.isSpectator());
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onPassiveDamage(EntityDamageEvent event) {
    Entity damagee = event.getEntity();
    if (!event.isCancelled() && damagee instanceof Player)
      filterMatchFromPlayer((Player) damagee).ifPresent((match) -> {
        PlayerMatchData data = SkywarsPlayer.getPlayer((Player) damagee).getMatchData();
        event.setCancelled(getPhase().isProtectionPhase() || data.isSpectator());
      });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onActiveDamage(EntityDamageByEntityEvent event) {
    Entity damager = event.getDamager();
    if (!event.isCancelled() && damager instanceof Player)
      filterMatchFromPlayer((Player) damager).ifPresent((match) -> {
        PlayerMatchData data = SkywarsPlayer.getPlayer((Player) damager).getMatchData();
        event.setCancelled(getPhase().isProtectionPhase() || data.isSpectator());
      });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onPickup(EntityPickupItemEvent event) {
    LivingEntity entity = event.getEntity();
    if (!event.isCancelled() && entity instanceof Player)
      filterMatchFromPlayer((Player) entity).ifPresent((match) -> {
        event.setCancelled(SkywarsPlayer.getPlayer((Player) entity).getMatchData().isSpectator());
      });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onFoodChange(FoodLevelChangeEvent event) {
    Entity entity = event.getEntity();
    if (!event.isCancelled() && entity instanceof Player)
      filterMatchFromPlayer((Player) entity).ifPresent((match) -> {
        PlayerMatchData data = SkywarsPlayer.getPlayer((Player) entity).getMatchData();
        event.setCancelled(getPhase().isProtectionPhase() || data.isSpectator());
      });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onDie(PlayerDeathEvent event) {
    Player entity = event.getEntity();
    filterMatchFromPlayer(entity).ifPresent((match) -> {
      Player killer = entity.getKiller();
      SkywarsPlayer player = SkywarsPlayer.getPlayer(entity);
      LazyVariableLookup map = new LazyVariableLookup();
      if (killer != null)
        ValueMapPopulators.populatePlayer(map, killer, ArrayPath.of("killer"));
      ValueMapPopulators.populatePlayer(map, entity, ArrayPath.of("player"));
      event.setDeathMessage(null);
      match.getAudience().sendFormattedMessage(killer != null
              ? MessageKeys.Match.KILLED
              : MessageKeys.Match.DIED,
          map);
      Bukkit.getScheduler().runTask(Skywars.plugin(), () -> {
        SkywarsSpectator.markAsSpectator(entity);
        entity.spigot().respawn();
        getPhase().evaluateGameEnd();
      });

      Team eliminated = player.getMatchData().getTeam();
      if (eliminated != null && eliminated.alive().count() > 1)
        eliminated = null;
      playDeathEffects(entity.getLocation(), eliminated);

      // Update the scoreboard to the dead one
      match.getScoreboardHandlers()
          .getHandler(MatchScoreboard.PLAYING_DEAD)
          .getOrCreateScoreboard(player)
          .show(player);
    });
  }

  private void playDeathEffects(@NonNull Location location, Team eliminated) {
    World world = location.getWorld();
    Preconditions.checkNotNull(world);
    world.strikeLightningEffect(location);
    if (eliminated != null) {
      // Play firework when a team has been eliminated
      Firework firework = world.spawn(location, Firework.class);
      FireworkMeta fireworkMeta = firework.getFireworkMeta();
      fireworkMeta.addEffect(FireworkEffect.builder()
          .withColor(eliminated.getTeamEnum().getColor())
          .withFade(Color.BLACK)
          .build());
      firework.setFireworkMeta(fireworkMeta);
      firework.detonate();
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onRespawn(PlayerRespawnEvent event) {
    Player entity = event.getPlayer();
    filterMatchFromPlayer(entity).ifPresent((match) -> {
      event.setRespawnLocation(match.getArena().getData().getSpectator());
      SkywarsSpectator.spawnAsSpectator(match, entity);
    });
  }

}
