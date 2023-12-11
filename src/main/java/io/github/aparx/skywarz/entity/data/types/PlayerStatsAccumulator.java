package io.github.aparx.skywarz.entity.data.types;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.database.stats.PlayerDatabaseStats;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-10 10:16
 * @since 1.0
 */
@Getter
public class PlayerStatsAccumulator {

  private static final Function<?, AtomicInteger> INTEGER_FACTORY = (__) -> new AtomicInteger();

  private final transient Object lock = new Object();

  private final @NonNull UUID id;

  @Getter(AccessLevel.NONE)
  private final EnumMap<PlayerStatsKey, AtomicInteger> statsMap =
      new EnumMap<>(PlayerStatsKey.class);

  public PlayerStatsAccumulator(
      @NonNull UUID id,
      @NonNull Map<? extends PlayerStatsKey, ? extends AtomicInteger> map) {
    Preconditions.checkNotNull(id, "ID must not be null");
    this.id = id;
    this.statsMap.putAll(map);
  }

  public PlayerStatsAccumulator(@NonNull UUID id) {
    this.id = id;
  }

  public boolean isEmpty() {
    return statsMap.isEmpty();
  }

  public void add(PlayerDatabaseStats databaseObject) {
    update(PlayerStatsKey.POINTS, (val) -> val.addAndGet(databaseObject.getPoints()));
    update(PlayerStatsKey.KILLS, (val) -> val.addAndGet(databaseObject.getKills()));
    update(PlayerStatsKey.DEATHS, (val) -> val.addAndGet(databaseObject.getDeaths()));
    update(PlayerStatsKey.PLAYED, (val) -> val.addAndGet(databaseObject.getMatchesPlayed()));
    update(PlayerStatsKey.WON, (val) -> val.addAndGet(databaseObject.getMatchesWon()));
  }

  @Synchronized("lock")
  public void update(Consumer<PlayerStatsAccumulator> updater) {
    updater.accept(this);
  }

  @Synchronized("lock")
  public void update(@NonNull PlayerStatsKey key, Consumer<@NonNull AtomicInteger> updater) {
    Preconditions.checkNotNull(key, "Key must not be null");
    Preconditions.checkNotNull(updater, "Updater must not be null");
    updater.accept(get(key));
  }

  @Synchronized("lock")
  public void increment(@NonNull PlayerStatsKey key) {
    update(key, AtomicInteger::incrementAndGet);
  }

  @Synchronized("lock")
  public void increment(@NonNull PlayerStatsKey key, int delta) {
    update(key, (atomic) -> atomic.addAndGet(delta));
  }

  @Synchronized("lock")
  @SuppressWarnings("unchecked")
  public AtomicInteger get(@NonNull PlayerStatsKey key) {
    return statsMap.computeIfAbsent(key, (Function<PlayerStatsKey, AtomicInteger>) INTEGER_FACTORY);
  }

  public int findGet(@NonNull PlayerStatsKey key) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return find(key).map(AtomicInteger::get).orElse(0);
  }

  @Synchronized("lock")
  public Optional<AtomicInteger> find(@NonNull PlayerStatsKey key) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return Optional.ofNullable(statsMap.get(key));
  }

  @Synchronized("lock")
  public void set(@NonNull PlayerStatsKey key, @Nullable AtomicInteger value) {
    Preconditions.checkNotNull(key, "Key must not be null");
    statsMap.put(key, value);
  }

  public int getMatchesLost() {
    return find(PlayerStatsKey.PLAYED).map(AtomicInteger::get).orElse(0)
        - find(PlayerStatsKey.WON).map(AtomicInteger::get).orElse(0);
  }

  public double getKillDeathRatio() {
    double kills = findGet(PlayerStatsKey.KILLS);
    double deaths = findGet(PlayerStatsKey.DEATHS);
    return kills == 0 ? 0 : kills / Math.max(deaths, 1);
  }

  public double getWinChance() {
    double won = findGet(PlayerStatsKey.WON);
    double played = findGet(PlayerStatsKey.PLAYED);
    return won == 0 || played == 0 ? 0 : 100 * (won / played);
  }

  @Override
  public String toString() {
    return "PlayerStatsAccumulator{" +
        "id=" + id +
        ", statsMap=" + statsMap +
        '}';
  }
}
