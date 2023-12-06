package io.github.aparx.skywarz.game.phase.phases;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Magics;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.WeakPlayerGroup;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.game.arena.GameSettings;
import io.github.aparx.skywarz.game.chest.ChestHandler;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.game.phase.features.Spectator;
import io.github.aparx.skywarz.game.team.Team;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.game.team.TeamMap;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.language.ValueMapPopulators;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:58
 * @since 1.0
 */
public class PlayingPhase extends GamePhase {

  // TODO move to game settings
  private static final TickDuration PROTECTION_PHASE_TIME = TickDuration.of(TimeUnit.SECONDS, 60);

  public PlayingPhase(@NonNull GamePhaseCycler cycler) {
    super(MatchState.PLAYING, cycler, TickDuration.of(TimeUnit.HOURS, 15),
        TickDuration.of(TimeUnit.TICKS, 5));
  }

  @Override
  public void handleJoin(SkywarsPlayer player) {
    PlayerMatchData data = player.getMatchData();
    data.setSpectator(true);
    // Handle spectator join
    player.findOnline().ifPresent((e) -> Spectator.spawnAsSpectator(getMatch(), e));
  }

  @Override
  public void handleLeave(SkywarsPlayer player) {
    player.findOnline().ifPresent((e) -> Spectator.removeSpectator(getMatch(), e));
  }

  @Override
  protected void onStart() {
    super.onStart();
    Match match = getMatch();
    match.getAudience().online().forEach((player) -> dequeueOnError(player, () -> {
      PlayerMatchData matchData = player.getMatchData();
      player.findOnline().ifPresent((online) -> {
        PlayerSnapshot.ofReset(online).restore(online);
        if (matchData.isSpectator())
          Spectator.spawnAsSpectator(match, online);
      });
      if (!matchData.isSpectator() && !matchData.isInTeam())
        // Join player into team since they have not selected one
        Preconditions.checkState(getNextFreeTeam(match.getTeamMap()).add(player));
    }));
    spawnPlayers();
  }

  @Override
  protected void updateTick() {
    Match match = getMatch();
    WeakPlayerGroup audience = match.getAudience();
    // (1) determine all teams that are alive
    evaluateGameState();
  }

  void evaluateGameState() {
    int aliveCount = 0;
    Match match = getMatch();
    if (!match.isState(MatchState.PLAYING))
      return;
    TeamMap teamMap = match.getTeamMap();
    Iterator<Team> iterator = teamMap.iterator();
    List<Team> alive = new ArrayList<>();
    for (Team team; aliveCount < 2
        && iterator.hasNext()
        && (team = iterator.next()) != null; )
      if (team.alive().findAny().isPresent()) {
        alive.add(team);
        ++aliveCount;
      }
    if (aliveCount == 0) {
      // no player existing anymore, thus cancel
      stop(StopReason.UNKNOWN);
      Skywars.getInstance().getMatchManager().remove(match);
      Bukkit.broadcastMessage("No team one (draw?)");
    } else if (aliveCount == 1 && !Magics.isDevelopment() /* TODO temporarily */) {
      getCycler().cycleNext();
      Team team = alive.get(0);
      // TODO
      Bukkit.broadcastMessage("§eTeam " + Language.getInstance().getTeamName(team.getTeamEnum()) + " won!");
    }
  }

  /**
   * Teleports all players to their according team spawns.
   * <p>If an error occurs when evaluating where a player should spawn, they are dequeued.
   */
  void spawnPlayers() {
    Match match = getMatch();
    Map<TeamEnum, SpawnGroup> spawns = new HashMap<>();
    match.getTeamMap().stream()
        .map(Team::getTeamEnum)
        .forEach((team) -> match.getArena().getData().getSpawns(team)
            .ifPresent((group) -> spawns.put(team, group.copy())));
    // O(n*m)
    match.getTeamMap().forEach((team) -> {
      SpawnGroup spawnGroup = spawns.get(team.getTeamEnum());
      team.forEach((player) -> dequeueOnError(player, () -> {
        Integer[] spawnIds = spawnGroup.toKeyArray();
        int spawnId = spawnIds[ThreadLocalRandom.current().nextInt(spawnIds.length)];
        player.getOnline().teleport(spawnGroup.get(spawnId));
        spawnGroup.remove(spawnId);
      }));
    });
  }

  void dequeueOnError(SkywarsPlayer player, Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      findMatch().ifPresent((match) -> match.leave(player));
      player.sendMessage(Language.getInstance().substitute(MessageKeys.Match.ERROR_DEQUEUED));
    }
  }

  /** Returns the next available team with the least number of players */
  @NonNull Team getNextFreeTeam(TeamMap teamMap) {
    Team filtered = null;
    int leastSize = -1;
    for (Team team : teamMap)
      if (team.hasSpace() && (leastSize < 0 || team.size() < leastSize)) {
        leastSize = team.size();
        filtered = team;
      }
    Preconditions.checkNotNull(filtered, "Could not find team");
    return filtered;
  }

  boolean hasProtectionPhase() {
    return findMatch()
        .map((match) -> match.getArena().getData().getSettings().getFlags())
        .filter(GameSettings.Flag.PROTECTION_PHASE::isFlagged)
        .isPresent();
  }

  boolean isProtectionPhase() {
    return hasProtectionPhase() && getTicker().hasElapsed(PROTECTION_PHASE_TIME);
  }

  // EVENT HANDLERS

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
    if (target.getBlockX() != origin.getBlockX()
        || target.getBlockZ() != origin.getBlockZ())
      filterMatchFromPlayer(event.getPlayer()).ifPresent((match) -> {
        Arena source = match.getArena().getSource();
        if (source != null && !source.getData().getBox().isWithin(target)) {
          Location newLocation = origin.clone();
          newLocation.setY(target.getY());
          event.setTo(newLocation);
        }
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
        event.setCancelled(isProtectionPhase() || data.isSpectator());
      });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onActiveDamage(EntityDamageByEntityEvent event) {
    Entity damager = event.getDamager();
    if (!event.isCancelled() && damager instanceof Player)
      filterMatchFromPlayer((Player) damager).ifPresent((match) -> {
        PlayerMatchData data = SkywarsPlayer.getPlayer((Player) damager).getMatchData();
        event.setCancelled(isProtectionPhase() || data.isSpectator());
      });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onFoodChange(FoodLevelChangeEvent event) {
    Entity entity = event.getEntity();
    if (!event.isCancelled() && entity instanceof Player)
      filterMatchFromPlayer((Player) entity).ifPresent((match) -> {
        PlayerMatchData data = SkywarsPlayer.getPlayer((Player) entity).getMatchData();
        event.setCancelled(isProtectionPhase() || data.isSpectator());
      });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onDie(PlayerDeathEvent event) {
    Player entity = event.getEntity();
    filterMatchFromPlayer(entity).ifPresent((match) -> {
      Player killer = entity.getKiller();
      Map<String, Object> valueMap = new HashMap<>();
      if (killer != null)
        ValueMapPopulators.populatePlayer(valueMap, killer, ArrayPath.of("killer"));
      ValueMapPopulators.populatePlayer(valueMap, entity, ArrayPath.of("player"));
      event.setDeathMessage(null);
      match.getAudience().sendFormattedMessage(killer != null
              ? MessageKeys.Match.KILLED
              : MessageKeys.Match.DIED,
          valueMap);
      Bukkit.getScheduler().runTask(Skywars.plugin(), () -> {
        Spectator.markAsSpectator(entity);
        entity.spigot().respawn();
        evaluateGameState();
      });
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onRespawn(PlayerRespawnEvent event) {
    Player entity = event.getPlayer();
    filterMatchFromPlayer(entity).ifPresent((match) -> {
      event.setRespawnLocation(match.getArena().getData().getSpectator());
      Spectator.spawnAsSpectator(match, entity);
    });
  }

}
