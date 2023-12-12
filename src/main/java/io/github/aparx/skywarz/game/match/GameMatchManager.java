package io.github.aparx.skywarz.game.match;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.GamePlayer;
import io.github.aparx.skywarz.events.match.MatchCreateEvent;
import io.github.aparx.skywarz.events.match.MatchJoinEvent;
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
public final class GameMatchManager extends DefaultSkywarsHandler implements Iterable<GameMatch> {

  public static final Function<SkywarsArena, GameMatch> DEFAULT_MATCH_FACTORY =
      (arena) -> new GameMatch(UUID.randomUUID(), arena);

  private static final SkywarsMatchListener LISTENER = new SkywarsMatchListener();

  private final KeyValueSet<UUID, GameMatch> internalSet = KeyValueSets.ofSnowflake();
  private final WeakHashMap<SkywarsArena, GameMatch> byArena = new WeakHashMap<>();
  private final SetMultimap<World, GameMatch> byWorld =
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

  // TODO move to a different (more fitting) place
  @CanIgnoreReturnValue
  public GameMatch join(@NonNull GamePlayer player, @NonNull SkywarsArena arena) {
    GameMatchManager matchManager = Skywars.getInstance().getMatchManager();
    GameMatch match = matchManager.find(arena).orElseGet(() -> {
      GameMatch newMatch = GameMatchManager.DEFAULT_MATCH_FACTORY.apply(arena);
      Preconditions.checkState(matchManager.register(newMatch), "Could not register match");
      MatchCreateEvent createEvent = new MatchCreateEvent(newMatch);
      Bukkit.getPluginManager().callEvent(createEvent);
      Preconditions.checkState(!createEvent.isCancelled(), "Match creation was cancelled");
      return newMatch;
    });
    MatchJoinEvent joinEvent = new MatchJoinEvent(match, player.getOnline());
    Bukkit.getPluginManager().callEvent(joinEvent);
    Preconditions.checkState(!joinEvent.isCancelled(), "Join was cancelled");
    Preconditions.checkState(match.getState().isJoinable(), "Match is not joinable");
    Preconditions.checkState(match.join(player));
    return match;
  }

  public int size() {
    return internalSet.size();
  }

  public boolean isEmpty() {
    return internalSet.isEmpty();
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean register(@NonNull GameMatch match) {
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
  public GameMatch getOrCreate(
      @NonNull SkywarsArena arena,
      @NonNull Function<SkywarsArena, GameMatch> matchFactory) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    Preconditions.checkNotNull(matchFactory, "Factory must not be null");
    if (contains(arena)) return get(arena);
    GameMatch newMatch = matchFactory.apply(arena);
    Preconditions.checkState(register(newMatch), "Could not register match");
    return newMatch;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public GameMatch getOrCreate(@NonNull SkywarsArena arena) {
    return getOrCreate(arena, DEFAULT_MATCH_FACTORY);
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean add(@NonNull GameMatch match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    if (!internalSet.add(match)) return false;
    byArena.put(match.getArena().getSource(), match);
    byWorld.put(match.getArena().getData().getWorld(), match);
    return true;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean remove(@NonNull GameMatch match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    if (!internalSet.remove(match)) return false;
    byArena.remove(match.getArena().getSource());
    match.notifyRemoval();
    // TODO check if this actually would throw any exceptions long-term
    byWorld.removeAll(match.getArena().getData().getWorld());
    return true;
  }

  @Synchronized
  public Optional<GameMatch> find(@NonNull UUID matchId) {
    Preconditions.checkNotNull(matchId, "ID must not be null");
    return internalSet.find(matchId);
  }

  @Synchronized
  public Optional<GameMatch> find(@NonNull SkywarsArena arena) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    return Optional.ofNullable(byArena.get(arena));
  }

  @Synchronized
  public Set<GameMatch> find(@NonNull World world) {
    Preconditions.checkNotNull(world, "World must not be null");
    return byWorld.get(world);
  }

  @Synchronized
  @CanIgnoreReturnValue
  public GameMatch get(@NonNull UUID matchId) {
    return find(matchId).orElseThrow();
  }

  @Synchronized
  @CanIgnoreReturnValue
  public GameMatch get(@NonNull SkywarsArena arena) {
    return find(arena).orElseThrow();
  }

  @Synchronized
  @CanIgnoreReturnValue
  public Set<GameMatch> get(@NonNull World world) {
    return find(world);
  }

  @Synchronized
  public boolean contains(UUID matchId) {
    return matchId != null && internalSet.containsKey(matchId);
  }

  @Synchronized
  public boolean contains(GameMatch match) {
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
  public @NonNull Iterator<GameMatch> iterator() {
    return internalSet.iterator();
  }
}
