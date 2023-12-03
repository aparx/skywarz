package io.github.aparx.skywarz.game.match;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.handler.LockingSkywarsHandler;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import io.github.aparx.skywarz.utils.collection.KeyValueSets;
import lombok.Synchronized;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 06:26
 * @since 1.0
 */
public final class MatchManager extends LockingSkywarsHandler {

  private static final Function<Arena, Match> DEFAULT_MATCH_FACTORY =
      (arena) -> new Match(UUID.randomUUID(), arena);

  private final KeyValueSet<UUID, Match> internalSet = KeyValueSets.ofSnowflake();
  private final WeakHashMap<Arena, Match> matchByArena = new WeakHashMap<>();

  @Override
  @Synchronized
  protected void onUnload() {
    internalSet.clear();
    matchByArena.clear();
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean register(@NonNull Match match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    Preconditions.checkState(
        !contains(match.getArena().getSource()) && !match.isState(MatchState.DONE),
        "There already is a match running at this arena currently");
    return add(match);
  }

  @Synchronized
  @CanIgnoreReturnValue
  public Match getOrCreate(@NonNull Arena arena, @NonNull Function<Arena, Match> matchFactory) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    Preconditions.checkNotNull(matchFactory, "Factory must not be null");
    if (contains(arena)) return get(arena);
    Match newMatch = matchFactory.apply(arena);
    Preconditions.checkState(register(newMatch), "Could not register match");
    return newMatch;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public Match getOrCreate(@NonNull Arena arena) {
    return getOrCreate(arena, DEFAULT_MATCH_FACTORY);
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean add(@NonNull Match match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    if (!internalSet.add(match)) return false;
    matchByArena.put(match.getArena().getSource(), match);
    return true;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean remove(@NonNull Match match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    if (!internalSet.remove(match)) return false;
    matchByArena.remove(match.getArena().getSource());
    return true;
  }

  @Synchronized
  public Optional<Match> find(@NonNull UUID matchId) {
    Preconditions.checkNotNull(matchId, "ID must not be null");
    return internalSet.find(matchId);
  }

  @Synchronized
  public Optional<Match> find(@NonNull Arena arena) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    return Optional.ofNullable(matchByArena.get(arena));
  }

  @Synchronized
  @CanIgnoreReturnValue
  public Match get(@NonNull UUID matchId) {
    return find(matchId).orElseThrow();
  }

  @Synchronized
  @CanIgnoreReturnValue
  public Match get(@NonNull Arena arena) {
    return find(arena).orElseThrow();
  }

  @Synchronized
  public boolean contains(UUID matchId) {
    return matchId != null && internalSet.containsKey(matchId);
  }

  @Synchronized
  public boolean contains(Match match) {
    return match != null && internalSet.contains(match);
  }

  @Synchronized
  public boolean contains(Arena arena) {
    return arena != null && matchByArena.containsKey(arena);
  }

}
