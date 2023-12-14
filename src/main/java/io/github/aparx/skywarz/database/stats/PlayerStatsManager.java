package io.github.aparx.skywarz.database.stats;

import com.google.common.base.Preconditions;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import io.github.aparx.skywarz.database.DatabaseObjectManager;
import io.github.aparx.skywarz.database.SkywarsDatabase;
import io.github.aparx.skywarz.database.object.CachableLazyObject;
import io.github.aparx.skywarz.database.object.CachableLazyObjectRegister;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsAccumulator;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 09:23
 * @since 1.0
 */
@Getter
public final class PlayerStatsManager extends DatabaseObjectManager {

  public PlayerStatsManager(@NonNull SkywarsDatabase database) {
    super(database);
  }

  private final CachableLazyObjectRegister<UUID, PlayerStatsAccumulator> registry =
      new CachableLazyObjectRegister<>(TickDuration.of(TimeUnit.MINUTES, 30),
          (uuid) -> createStatsObjectWrapper(Duration.ofMinutes(5), uuid));

  public CachableLazyObject<PlayerStatsAccumulator>
  createStatsObjectWrapper(@NonNull Duration freshTime, @NonNull UUID playerId) {
    PlayerStatsAccumulator defaultObject = new PlayerStatsAccumulator(playerId);
    return CachableLazyObject.of(freshTime, () -> {
      if (getDatabase().isEnabled())
        return query(playerId).thenApply(stats -> stats != null
            ? stats.accumulate()
            : defaultObject);
      return null;
    }, defaultObject);
  }

  @Override
  public CompletableFuture<Void> register() throws SQLException {
    SkywarsDatabase database = getDatabase();
    Preconditions.checkState(database.isEnabled(), "Database is not enabled");
    registry.register();
    DaoManager.createDao(database.getSource(), PlayerDatabaseStats.class);
    return database.executeAsync(() -> {
      TableUtils.createTableIfNotExists(database.getSource(), PlayerDatabaseStats.class);
    });
  }

  @Override
  public void unregister() {
    registry.unregister();
  }

  public Dao<PlayerDatabaseStats, UUID> getStatsDao() {
    return DaoManager.lookupDao(getDatabase().getSource(), PlayerDatabaseStats.class);
  }

  public CompletableFuture<Integer> delete(@NonNull UUID uuid) {
    return getDatabase().executeAsync(() -> getStatsDao().deleteById(uuid))
        .thenApply((rowsAffected) -> {
          getRegistry().remove(uuid);
          return rowsAffected;
        });
  }

  public CompletableFuture<Dao.CreateOrUpdateStatus> apply(@NonNull PlayerStatsAccumulator data) {
    return query(data.getId()).thenCompose((stats) -> {
      stats = stats != null ? stats : new PlayerDatabaseStats();
      stats.setId(data.getId());
      stats.add(data);
      return update(stats);
    }).thenApply((status) -> {
      // force a re-fetch of the data next time a user requests this data
      registry.find(data.getId()).ifPresent(CachableLazyObject::purge);
      return status;
    });
  }

  public CompletableFuture<Dao.CreateOrUpdateStatus> update(@NonNull PlayerDatabaseStats stats) {
    Preconditions.checkNotNull(stats, "Stats must not be null");
    Preconditions.checkState(getDatabase().isEnabled(), "Database is not enabled");
    return getDatabase().executeAsync(() -> getStatsDao().createOrUpdate(stats));
  }

  public CompletableFuture<PlayerDatabaseStats> createIfNotExists(@NonNull PlayerDatabaseStats stats) {
    Preconditions.checkNotNull(stats, "Stats must not be null");
    Preconditions.checkState(getDatabase().isEnabled(), "Database is not enabled");
    return getDatabase().executeAsync(() -> getStatsDao().createIfNotExists(stats));
  }

  public CompletableFuture<@Nullable PlayerDatabaseStats> query(@NonNull UUID uuid) {
    Preconditions.checkState(getDatabase().isEnabled(), "Database is not enabled");
    return getDatabase().executeAsync(() -> getStatsDao().queryForId(uuid));
  }
}
