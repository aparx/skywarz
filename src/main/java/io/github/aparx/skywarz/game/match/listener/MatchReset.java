package io.github.aparx.skywarz.game.match.listener;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.game.arena.reset.ArenaReset;
import io.github.aparx.skywarz.game.match.Match;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 08:38
 * @since 1.0
 */
public final class MatchReset implements Listener {

  private final WeakReference<Match> match;

  public MatchReset(@NonNull Match match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    this.match = new WeakReference<>(match);
  }

  public void register() {
    Bukkit.getPluginManager().registerEvents(this, Skywars.plugin());
  }

  public void unregister() {
    HandlerList.unregisterAll(this);
  }

  public Optional<Match> findMatch() {
    return Optional.ofNullable(match.get());
  }

  public Match getMatch() {
    return findMatch().orElseThrow();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onBlockBreak(BlockBreakEvent event) {
    if (event.isCancelled()) return;
    handle(event.getPlayer(), (player, reset) -> {
      // TODO add whole connected structure (note: is that needed?)
      reset.addSnapshot(new ArenaReset.LocationSnapshot(
          event.getBlock().getLocation(), event.getBlock().getBlockData()
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onBlockPlace(BlockPlaceEvent event) {
    if (event.isCancelled()) return;
    BlockState blockReplacedState = event.getBlockReplacedState();
    handle(event.getPlayer(), (player, reset) -> {
      reset.addSnapshot(new ArenaReset.LocationSnapshot(
          event.getBlock().getLocation(), blockReplacedState.getBlockData()
      ));
    });
  }

  void handle(Player entity, BiConsumer<SkywarsPlayer, ArenaReset> callback) {
    findMatch()
        .flatMap(match -> SkywarsPlayer.findPlayer(entity))
        .ifPresent((player) -> {
          Match match = getMatch();
          if (match.equals(player.getMatchData().getMatch())) {
            Arena source = match.getArena().getSource();
            if (source != null)
              callback.accept(player, source.getReset());
          }
        });
  }
}