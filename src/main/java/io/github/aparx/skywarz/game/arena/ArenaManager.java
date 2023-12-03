package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.bufig.Config;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import io.github.aparx.skywarz.handler.LockingSkywarsHandler;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import io.github.aparx.skywarz.utils.collection.KeyValueSets;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 02:51
 * @since 1.0
 */
public final class ArenaManager extends LockingSkywarsHandler implements Iterable<Arena> {

  private final KeyValueSet<String, Arena> internalSet =
      KeyValueSets.of((arena) -> arena.getName().toLowerCase());

  private static @NonNull Config getConfig() {
    return Skywars.getInstance().getConfigHandler().getArenas();
  }

  @Override
  protected void onLoad() {
    Config config = getConfig();
    config.setHeader(SkywarsConfigHandler.createHeader("All saved arenas (Do not touch!)"));
    config.load();
    final Logger logger = Skywars.logger();
    config.getKeys(false).stream()
        .map((key) -> (Arena) config.get(key))
        .filter(internalSet::add)
        .map(Arena::getName)
        .forEach((arena) -> logger.log(Level.INFO, "Located arena: {0}", arena));
  }

  @Override
  protected void onUnload() {
    internalSet.clear();
  }

  public void register(@NonNull Arena arena) {
    synchronized (handlerLock) {
      Preconditions.checkNotNull(arena, "Arena must not be null");
      Preconditions.checkState(internalSet.add(arena), "Could not add arena (duplicate?)");
      Skywars.logger().log(Level.INFO, "Registered arena: {0}", arena.getName());
      saveArena(arena);
    }
  }

  @CanIgnoreReturnValue
  public boolean delete(@NonNull Arena arena) {
    synchronized (handlerLock) {
      Preconditions.checkNotNull(arena, "Arena must not be null");
      if (!internalSet.remove(arena)) return false;
      Skywars.logger().log(Level.INFO, "Deleted arena: {0}", arena.getName());
      setInConfigAtArena(arena, null).save();
      return true;
    }
  }

  @CanIgnoreReturnValue
  public boolean delete(@NonNull String name) {
    synchronized (handlerLock) {
      Preconditions.checkNotNull(name, "Name must not be null");
      return find(name).filter(this::delete).isPresent();
    }
  }

  public void saveArena(@NonNull Arena arena) {
    setInConfigAtArena(arena, arena).save();
  }

  @CheckReturnValue
  public Optional<Arena> find(@NonNull String name) {
    Preconditions.checkNotNull(name, "Name must not be null");
    synchronized (handlerLock) {
      return internalSet.find(name);
    }
  }

  @CanIgnoreReturnValue
  public @NonNull Arena get(@NonNull String name) {
    Preconditions.checkNotNull(name, "Name must not be null");
    synchronized (handlerLock) {
      return internalSet.require(name);
    }
  }

  public boolean contains(String name) {
    synchronized (handlerLock) {
      return internalSet.containsKey(name);
    }
  }

  public boolean contains(Arena arena) {
    synchronized (handlerLock) {
      return internalSet.contains(arena);
    }
  }

  public @NonNull Stream<Arena> stream() {
    return internalSet.stream();
  }

  @Override
  public @NonNull Iterator<Arena> iterator() {
    return internalSet.iterator();
  }

  public Set<Arena> asSet() {
    return internalSet;
  }

  @CanIgnoreReturnValue
  private Config setInConfigAtArena(Arena arena, Object value) {
    Config config = getConfig();
    config.set(internalSet.getKey(arena), value);
    return config;
  }
}
