package io.github.aparx.skywarz.game.match;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.game.match.listener.SkywarsMatchListener;
import io.github.aparx.skywarz.handler.DefaultSkywarsHandler;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import io.github.aparx.skywarz.utils.collection.KeyValueSets;
import io.github.aparx.skywarz.utils.collection.WeakHashSet;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 06:26
 * @since 1.0
 */
public final class SkywarsMatchManager extends DefaultSkywarsHandler implements Iterable<SkywarsMatch> {

  public static final Function<SkywarsArena, SkywarsMatch> DEFAULT_MATCH_FACTORY =
      (arena) -> new SkywarsMatch(UUID.randomUUID(), arena);

  private static final SkywarsMatchListener LISTENER = new SkywarsMatchListener();

  private final KeyValueSet<UUID, SkywarsMatch> internalSet = KeyValueSets.ofSnowflake();
  private final WeakHashMap<SkywarsArena, SkywarsMatch> byArena = new WeakHashMap<>();
  private final SetMultimap<World, SkywarsMatch> byWorld =
      Multimaps.newSetMultimap(new WeakHashMap<>(), WeakHashSet::new);

  @Override
  protected void onLoad() {
    Bukkit.getPluginManager().registerEvents(LISTENER, Skywars.plugin());
  }

  @Override
  @Synchronized
  protected void onUnload() {
    new ArrayList<>(internalSet).forEach(this::remove);

    internalSet.clear();
    byArena.clear();
    HandlerList.unregisterAll(LISTENER);
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean register(@NonNull SkywarsMatch match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    SkywarsArena source = match.getArena().getSource();
    Preconditions.checkNotNull(source, "Arena source is invalid");
    Preconditions.checkState(!source.isAcquiredByMatch(),
        "There already is a match running at this arena currently");
    if (!add(match)) return false;
    match.notifyRegister();
    return true;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public SkywarsMatch getOrCreate(
      @NonNull SkywarsArena arena,
      @NonNull Function<SkywarsArena, SkywarsMatch> matchFactory) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    Preconditions.checkNotNull(matchFactory, "Factory must not be null");
    if (contains(arena)) return get(arena);
    SkywarsMatch newMatch = matchFactory.apply(arena);
    Preconditions.checkState(register(newMatch), "Could not register match");
    return newMatch;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public SkywarsMatch getOrCreate(@NonNull SkywarsArena arena) {
    return getOrCreate(arena, DEFAULT_MATCH_FACTORY);
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean add(@NonNull SkywarsMatch match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    if (!internalSet.add(match)) return false;
    byArena.put(match.getArena().getSource(), match);
    byWorld.put(match.getArena().getData().getWorld(), match);
    return true;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean remove(@NonNull SkywarsMatch match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    if (!internalSet.remove(match)) return false;
    byArena.remove(match.getArena().getSource());
    match.notifyRemoval();
    // TODO check if this actually would throw any exceptions long-term
    byWorld.removeAll(match.getArena().getData().getWorld());
    return true;
  }

  @Synchronized
  public Optional<SkywarsMatch> find(@NonNull UUID matchId) {
    Preconditions.checkNotNull(matchId, "ID must not be null");
    return internalSet.find(matchId);
  }

  @Synchronized
  public Optional<SkywarsMatch> find(@NonNull SkywarsArena arena) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    return Optional.ofNullable(byArena.get(arena));
  }

  @Synchronized
  public Set<SkywarsMatch> find(@NonNull World world) {
    Preconditions.checkNotNull(world, "World must not be null");
    return byWorld.get(world);
  }

  @Synchronized
  @CanIgnoreReturnValue
  public SkywarsMatch get(@NonNull UUID matchId) {
    return find(matchId).orElseThrow();
  }

  @Synchronized
  @CanIgnoreReturnValue
  public SkywarsMatch get(@NonNull SkywarsArena arena) {
    return find(arena).orElseThrow();
  }

  @Synchronized
  @CanIgnoreReturnValue
  public Set<SkywarsMatch> get(@NonNull World world) {
    return find(world);
  }

  @Synchronized
  public boolean contains(UUID matchId) {
    return matchId != null && internalSet.containsKey(matchId);
  }

  @Synchronized
  public boolean contains(SkywarsMatch match) {
    return match != null && internalSet.contains(match);
  }

  @Synchronized
  public boolean contains(SkywarsArena arena) {
    return arena != null && byArena.containsKey(arena);
  }

  @Synchronized
  public boolean contains(World world) {
    return world != null && byWorld.containsKey(world) && !byWorld.get(world).isEmpty();
  }

  @Override
  public @NonNull Iterator<SkywarsMatch> iterator() {
    return internalSet.iterator();
  }
}
