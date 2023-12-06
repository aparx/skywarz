package io.github.aparx.skywarz.game.match.listener;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.game.arena.reset.ArenaReset;
import io.github.aparx.skywarz.utils.material.ConnectedMaterial;
import io.github.aparx.skywarz.game.arena.snapshot.ArenaSnapshot;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 02:54
 * @since 1.0
 */
public class MatchListener implements Listener {

  @EventHandler
  void onQuit(PlayerQuitEvent event) {
    SkywarsPlayer.findPlayer(event.getPlayer()).ifPresent((player) -> {
      PlayerMatchData matchData = player.getMatchData();
      Match match = matchData.getMatch();
      if (matchData.isInMatch() && match != null)
        match.leave(player);
    });
  }

  // Map reset

  @EventHandler(priority = EventPriority.HIGHEST)
  void onBlockExplode(BlockExplodeEvent event) {
    if (!event.isCancelled())
      addResetHandle(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onEntityExplode(EntityExplodeEvent event) {
    if (event.isCancelled()) return;
    List<Location> added = new ArrayList<>();
    handle(event.getEntity().getWorld(), event.blockList(), (block, arena) -> {
      if (!added.isEmpty() || arena.getData().getBox().isWithin(event.getEntity().getBoundingBox()))
        // only force the removal of blocks within the event if the entity itself is in the arena
        added.add(block.getLocation());
      arena.getReset().addStructure(new ArenaReset.LocationSnapshot(
          block.getLocation(), block.getBlockData().clone()
      ));
    });
    if (!added.isEmpty())
      // at least one block is within an arena: remove all blocks outside the arena
      event.blockList().removeAll(event.blockList().stream()
          .filter((block) -> !added.contains(block.getLocation()))
          .collect(Collectors.toList()));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onBlockBreak(BlockBreakEvent event) {
    if (event.isCancelled()) return;
    handle(event.getPlayer(), (source, reset) -> {
      Location location = event.getBlock().getLocation();
      if (!source.getData().getBox().isWithin(location))
        event.setCancelled(true);
      else
        reset.addStructure(new ArenaReset.LocationSnapshot(
            event.getBlock().getLocation(), event.getBlock().getBlockData().clone()
        ));
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onBlockPlace(BlockPlaceEvent event) {
    if (event.isCancelled()) return;
    BlockState blockReplacedState = event.getBlockReplacedState();
    handle(event.getPlayer(), (source, reset) -> {
      Location location = event.getBlock().getLocation();
      if (!source.getData().getBox().isWithin(location))
        event.setCancelled(true);
      else {
        ArenaReset.LocationSnapshot snapshot = new ArenaReset.LocationSnapshot(
            location, blockReplacedState.getBlockData().clone());
        if (ConnectedMaterial.isStructure(blockReplacedState.getType()))
          reset.addStructure(snapshot);
        else reset.addSnapshot(snapshot);
      }
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onInteract(PlayerInteractEvent event) {
    Block clickedBlock = event.getClickedBlock();
    if (clickedBlock == null) return;
    handle(event.getPlayer(), (source, reset) -> {
      if (!ConnectedMaterial.isStructure(event.getMaterial())
          && event.getMaterial().isSolid())
        return;
      Block relative = clickedBlock.getRelative(event.getBlockFace());
      reset.addStructure(new ArenaReset.LocationSnapshot(
          relative.getLocation(), relative.getBlockData().clone()
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onFromTo(BlockFromToEvent event) {
    if (!event.isCancelled())
      addResetHandle(event.getToBlock());
  }

  void addResetHandle(Block block) {
    handle(block, (reset) -> reset.addStructure(
        new ArenaReset.LocationSnapshot(block.getLocation(), block.getBlockData().clone())
    ));
  }

  void handle(World world, List<Block> blocks, BiConsumer<Block, Arena> callback) {
    Skywars.getInstance().getMatchManager().forEach((match) -> {
      ArenaSnapshot arena = match.getArena();
      World arenaWorld = arena.getData().getWorld();
      Arena source = arena.getSource();
      blocks.forEach((block) -> {
        if (block.getWorld() != world) return;
        Location location = block.getLocation();
        if (world.equals(arenaWorld) && source != null
            && source.getData().getBox().isWithin(location))
          callback.accept(block, source);
      });
    });
  }

  void handle(Block block, Consumer<ArenaReset> callback) {
    handle(block.getWorld(), List.of(block), (b, arena) -> callback.accept(arena.getReset()));
  }

  void handle(Player entity, BiConsumer<Arena, ArenaReset> callback) {
    SkywarsPlayer.findPlayer(entity).ifPresent((player) -> {
      PlayerMatchData data = player.getMatchData();
      Match match = data.getMatch();
      if (data.isInMatch() && match != null
          && !match.isState(MatchState.SETUP)
          && !match.isState(MatchState.DONE)) {
        Arena source = match.getArena().getSource();
        if (source != null)
          callback.accept(source, source.getReset());
      }
    });
  }

}
