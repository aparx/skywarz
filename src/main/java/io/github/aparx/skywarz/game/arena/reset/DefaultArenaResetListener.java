package io.github.aparx.skywarz.game.arena.reset;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.game.arena.ArenaBox;
import io.github.aparx.skywarz.game.arena.snapshot.ArenaDataSnapshot;
import io.github.aparx.skywarz.game.arena.snapshot.ArenaSnapshot;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.match.GameMatchState;
import io.github.aparx.skywarz.utils.collection.WeakHashSet;
import io.github.aparx.skywarz.utils.material.MaterialTag;
import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 08:38
 * @since 1.0
 */
@Getter
public final class DefaultArenaResetListener implements Listener {

  private final DefaultArenaReset reset;

  private final WeakHashSet<Entity> entityBlockChanges = new WeakHashSet<>();

  public DefaultArenaResetListener(@NonNull DefaultArenaReset reset) {
    Preconditions.checkNotNull(reset);
    this.reset = reset;
  }

  public @NonNull GameArena getArena() {
    try {
      return reset.getArena();
    } catch (Exception e) {
      HandlerList.unregisterAll(this);
      throw e;
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onEntityChange(EntityChangeBlockEvent event) {
    if (!event.isCancelled())
      // this only captures entity/block changes within the arena. If a block falls out of
      // the arena, it will not be captured. TODO: maybe implement a tracking of the entity
      handle(event.getBlock(), () -> {
        getReset().addStructure(DefaultArenaReset.BlockSnapshot.take(event.getBlock()));
      });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onBlockExplode(BlockExplodeEvent event) {
    if (!event.isCancelled())
      addResetHandle(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onEntityExplode(EntityExplodeEvent event) {
    if (event.isCancelled()) return;
    List<Location> added = new ArrayList<>();
    handle(event.getEntity().getWorld(), event.blockList(), (block, match) -> {
      GameArena arena = match.getArena().getSource();
      Preconditions.checkNotNull(arena, "Source became invalid");
      if (!added.isEmpty() || arena.getData().getBox().isWithin(event.getEntity().getBoundingBox()))
        // only force the removal of blocks within the event if the entity itself is in the arena
        added.add(block.getLocation());
      reset.addStructure(DefaultArenaReset.BlockSnapshot.take(block));
    });
    if (!added.isEmpty())
      // at least one block is within an arena: remove all blocks outside the arena
      event.blockList().removeAll(event.blockList().stream()
          .filter((block) -> !added.contains(block.getLocation()))
          .collect(Collectors.toList()));
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onBlockBreak(BlockBreakEvent event) {
    if (event.isCancelled()) return;
    handle(event.getPlayer(), (match, arena) -> {
      Location location = event.getBlock().getLocation();
      if (!arena.getData().getBox().isWithin(location))
        event.setCancelled(true);
      else
        reset.addStructure(new DefaultArenaReset.BlockSnapshot(
            event.getBlock().getLocation(), event.getBlock().getBlockData().clone()
        ));
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onBlockPlace(BlockPlaceEvent event) {
    if (event.isCancelled()) return;
    BlockState blockReplacedState = event.getBlockReplacedState();
    handle(event.getPlayer(), (match, arena) -> {
      Location location = event.getBlock().getLocation();
      if (!arena.getData().getBox().isWithin(location))
        event.setCancelled(true);
      else {
        DefaultArenaReset.BlockSnapshot snapshot = new DefaultArenaReset.BlockSnapshot(
            location, blockReplacedState.getBlockData().clone());
        if (MaterialTag.connected.isTagged(blockReplacedState.getType()))
          reset.addStructure(snapshot);
        else reset.addSnapshot(snapshot);
      }
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onInteract(PlayerInteractEvent event) {
    Block clickedBlock = event.getClickedBlock();
    if (clickedBlock == null) return;
    handle(event.getPlayer(), (match, arena) -> {
      ArenaBox box = arena.getData().getBox();
      if (!box.isWithin(clickedBlock.getBoundingBox()))
        event.setCancelled(true);
      ItemStack item = event.getItem();
      Action action = event.getAction();
      if (item == null) return;
      Block relative = clickedBlock.getRelative(event.getBlockFace());
      // add the block for when a bucket is emptied / painting is destroyed
      if (!box.isWithin(relative.getLocation()))
        event.setCancelled(true);
      else if (action == Action.RIGHT_CLICK_BLOCK
          && MaterialTag.emptyableBucket.isTagged(item.getType())) {
        reset.addStructure(DefaultArenaReset.BlockSnapshot.take(relative));
        reset.addSnapshot(DefaultArenaReset.BlockSnapshot.take(clickedBlock));
      }
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onInteract(PlayerInteractEntityEvent event) {
    if (event.getRightClicked() instanceof ItemFrame)
      handle(event.getPlayer(), (match, arena) -> event.setCancelled(true));
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onBlockTravel(BlockFromToEvent event) {
    if (!event.isCancelled())
      event.setCancelled(addResetHandle(event.getBlock())
          != addResetHandle(event.getToBlock()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onBlockBurn(BlockBurnEvent event) {
    if (!event.isCancelled())
      addResetHandle(event.getBlock());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onBlockForm(BlockFormEvent event) {
    if (!event.isCancelled())
      addResetHandle(event.getBlock());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onFireSpread(BlockSpreadEvent event) {
    if (!event.isCancelled())
      addResetHandle(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onDestroyHanging(HangingBreakByEntityEvent event) {
    Entity remover = event.getRemover();
    Hanging entity = event.getEntity();
    if (!event.isCancelled())
      handle(entity.getLocation(), (match, location) -> event.setCancelled(true));
    if (!event.isCancelled() && remover instanceof Player)
      handle((Player) remover, (match, arena) -> event.setCancelled(true));
  }

  @CanIgnoreReturnValue
  boolean addResetHandle(Block block) {
    AtomicBoolean contained = new AtomicBoolean();
    handle(block, () -> {
      contained.set(true);
      reset.addStructure(DefaultArenaReset.BlockSnapshot.take(block));
    });
    return contained.get();
  }

  private Optional<GameMatch> findMatch() {
    return Skywars.getInstance().getMatchManager().find(getArena());
  }

  void handle(World world, List<Block> blocks, BiConsumer<Block, GameMatch> callback) {
    findMatch().ifPresent((match) -> {
      ArenaSnapshot arena = match.getArena();
      GameArena source = arena.getSource();
      if (world.equals(match.getArena().getData().getWorld()))
        blocks.forEach((block) -> {
          Preconditions.checkState(block.getWorld().equals(world));
          Location location = block.getLocation();
          if (source != null && source.getData().getBox().isWithin(location))
            callback.accept(block, match);
        });
    });
  }

  void handle(Block block, Runnable callback) {
    handle(block.getWorld(), List.of(block), (b, match) -> {
      GameArena source = match.getArena().getSource();
      Preconditions.checkNotNull(source, "Source became invalid");
      callback.run();
    });
  }

  void handle(Player entity, BiConsumer<GameMatch, GameArena> callback) {
    SkywarsPlayer.findPlayer(entity).ifPresent((player) -> {
      PlayerMatchData data = player.getMatchData();
      findMatch()
          .filter((x) -> data.isInMatch() && x.equals(data.getMatch()))
          .filter((x) -> x.getState().isAfterOrEqual(GameMatchState.PLAYING))
          .ifPresent((match) -> {
            GameArena source = match.getArena().getSource();
            if (source != null) callback.accept(match, source);
          });
    });
  }

  void handle(Location location, BiConsumer<GameMatch, Location> callback) {
    handle(new Location[]{location}, callback);
  }

  void handle(Location[] locations, BiConsumer<GameMatch, Location> callback) {
    if (ArrayUtils.isEmpty(locations)) return;
    findMatch().ifPresent((match) -> {
      for (Location location : locations) {
        ArenaDataSnapshot data = match.getArena().getData();
        if (data.getWorld().equals(location.getWorld()))
          if (match.getState().isAfterOrEqual(GameMatchState.PLAYING)
              && data.getBox().isWithin(location))
            callback.accept(match, location);
      }
    });
  }

}