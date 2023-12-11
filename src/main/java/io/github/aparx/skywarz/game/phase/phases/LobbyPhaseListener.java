package io.github.aparx.skywarz.game.phase.phases;

import io.github.aparx.skywarz.game.phase.SkywarsPhase;
import io.github.aparx.skywarz.game.phase.SkywarsPhaseListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-07 10:49
 * @since 1.0
 */
public class LobbyPhaseListener extends SkywarsPhaseListener<SkywarsPhase> {

  public LobbyPhaseListener(@NonNull SkywarsPhase phase) {
    super(phase);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onInteract(PlayerInteractEvent event) {
    if (event.useItemInHand() != Event.Result.DENY)
      event.setCancelled(filterMatchFromPlayer(event.getPlayer()).isPresent());
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

  @EventHandler(priority = EventPriority.HIGH)
  void onPickup(EntityPickupItemEvent event) {
    if (event.isCancelled()) return;
    if (event.getEntity() instanceof Player)
      event.setCancelled(filterMatchFromPlayer((Player) event.getEntity()).isPresent());
  }
}
