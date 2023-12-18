package io.github.aparx.skywarz.game.arena.sign;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.bufig.handler.ConfigProxy;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import io.github.aparx.skywarz.language.LazyVariableLookup;
import io.github.aparx.skywarz.language.VariablePopulator;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import io.github.aparx.skywarz.startup.Main;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-10 18:52
 * @since 1.0
 */
public final class ArenaSignHandler implements Listener {

  @Getter
  private static final ConfigProxy templateConfigProxy = new ConfigProxy((proxy) -> {
    return Skywars.getInstance().getConfigHandler().getOrCreate("sign");
  });

  private static final List<String> DEFAULT_LINES = List.of(
      "{state.color}§l[SW]",
      "{state.color}{state.name}",
      "{name}",
      "{alive}/{maxPlayers}"
  );

  private final WeakReference<GameArena> arena;

  @Getter
  private SkywarsSignRegister register;

  @Getter
  private List<String> template;

  public ArenaSignHandler(@NonNull GameArena arena) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    this.arena = new WeakReference<>(arena);
    Bukkit.getPluginManager().registerEvents(this, Skywars.plugin());
  }

  public void update() {
    GameArena arena = getArena();
    LazyVariableLookup lookup = createLookup();
    Collection<ArenaSign> collection = getRegister().getCollection();
    collection.stream()
        .filter((sign) -> !sign.update(lookup, arena))
        .collect(Collectors.toList())
        .forEach(collection::remove);
  }

  public void load() {
    templateConfigProxy.load();
    templateConfigProxy.setHeaderIfAbsent(SkywarsConfigHandler.createHeader(
        "Sign configuration",
        "Edit to update the sign layout of Skywarz signs.",
        "Note: as of the version (see below) these signs only work for multi-arena."
    ));
    // load sign lines
    Object linesObject = templateConfigProxy.get("lines");
    this.template = linesObject instanceof Collection
        ? ((Collection<?>) linesObject).stream()
        .map(Objects::toString)
        .collect(Collectors.toList())
        : DEFAULT_LINES;
    // load registered signs
    Object registerObject = getArena().get("signs");
    this.register = registerObject instanceof SkywarsSignRegister
        ? (SkywarsSignRegister) registerObject
        : new SkywarsSignRegister();
    templateConfigProxy.save();
  }

  public void save() {
    // save sign lines
    if (template != null && !template.isEmpty())
      templateConfigProxy.set("lines", template);
    // save registered signs
    if (register != null)
      getArena().set("signs", register);
    templateConfigProxy.save();
  }

  public LazyVariableLookup createLookup() {
    LazyVariableLookup lookup = new LazyVariableLookup();
    VariablePopulator.addArenaOrAcquiree(lookup, getArena(), ArrayPath.of());
    return lookup;
  }

  public @NonNull Optional<GameArena> findArena() {
    return Optional.ofNullable(arena.get());
  }

  public @NonNull GameArena getArena() {
    GameArena skywarsArena = arena.get();
    Preconditions.checkState(skywarsArena != null, "Arena has become invalid");
    return skywarsArena;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onSignCreate(PlayerInteractEvent event) {
    Block clickedBlock = event.getClickedBlock();
    if (event.useInteractedBlock() == Event.Result.DENY || clickedBlock == null
        || event.getAction() != Action.RIGHT_CLICK_BLOCK)
      return;
    BlockState state = clickedBlock.getState();
    if (!(state instanceof Sign)) return;
    findArena().ifPresentOrElse((arena) -> {
      Player entity = event.getPlayer();
      if (arena.getSignHandler().getRegister().getCollection().containsKey(state.getLocation())) {
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(Skywars.plugin(), () -> {
          if (entity.isValid())
            entity.performCommand(String.format("%s join %s", Main.FULL_COMMAND, arena.getName()));
        });
      }
    }, () -> HandlerList.unregisterAll(this));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onSignCreate(SignChangeEvent event) {
    Player player = event.getPlayer();
    if (event.isCancelled()) return;
    findArena().ifPresentOrElse((arena) -> {
      if ("[SW]".equalsIgnoreCase(event.getLine(0))
          && SkywarsPermission.SETUP.has(player)
          && arena.getName().equalsIgnoreCase(event.getLine(1))) {
        ArenaSign newSign = new ArenaSign(event.getBlock().getState());
        getRegister().getCollection().add(newSign);
        arena.save();
        event.setCancelled(true);
        newSign.update(createLookup(), arena);
      }
    }, () -> HandlerList.unregisterAll(this));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onSignBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    if (event.isCancelled()) return;
    BlockState state = event.getBlock().getState();
    if (!(state instanceof Sign)) return;
    Location location = state.getLocation();
    KeyValueSet<Location, ArenaSign> collection = getRegister().getCollection();
    findArena()
        .filter((pass) -> collection.containsKey(location))
        .ifPresent((arena) -> {
          if (SkywarsPermission.SETUP.has(player)
              && collection.remove(collection.get(location))) {
            Objects.requireNonNull(state.getWorld()).spawnParticle(
                Particle.SMOKE_NORMAL, location.clone().add(0.5, 0.5, 0.5),
                4, 0, 0, 0, 0);
            save();
          } else event.setCancelled(true);
        });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onExplode(BlockExplodeEvent event) {
    if (event.isCancelled()) return;
    BlockState state = event.getBlock().getState();
    if (state instanceof Sign) findArena()
        .filter((pass) -> getRegister().getCollection().containsKey(state.getLocation()))
        .ifPresent((arena) -> event.setCancelled(true));
  }
}
