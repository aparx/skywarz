package io.github.aparx.skywarz.database.leaderboard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.j256.ormlite.stmt.QueryBuilder;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.database.object.CachableLazyObject;
import io.github.aparx.skywarz.database.stats.PlayerDatabaseStats;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsAccumulator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-14 16:14
 * @since 1.0
 */
@RequiredArgsConstructor
public final class Leaderboard {

  /** The main leaderboard contains up to ten entries (meaning the Top 10) by default. */
  @Getter
  private static final Leaderboard mainLeaderboard = new Leaderboard(0, 10L);

  @Getter
  private final CachableLazyObject<List<PlayerStatsAccumulator>> content =
      CachableLazyObject.of(Duration.ofHours(1), () ->
              executeQuery().thenApply((list) -> list.stream()
                  .map(PlayerDatabaseStats::accumulate)
                  .collect(ImmutableList.toImmutableList())),
          ImmutableList.of());

  private final long offset;
  private final long limit;

  public CompletableFuture<List<PlayerDatabaseStats>> executeQuery() {
    return Skywars.getInstance().getDatabase().executeAsync(() -> {
      QueryBuilder<PlayerDatabaseStats, UUID> queryBuilder = createQuery();
      if (offset >= 1) queryBuilder.offset(offset);
      return queryBuilder.limit(limit).query();
    });
  }

  public QueryBuilder<PlayerDatabaseStats, UUID> createQuery() {
    return Skywars.getInstance().getDatabase().getStatsManager().getStatsDao()
        .queryBuilder().orderBy("points", false);
  }

  public List<PlayerStatsAccumulator> createView(
      int fromInclusiveIndex, int maxExclusiveIndex) {
    Preconditions.checkState(fromInclusiveIndex >= maxExclusiveIndex,
        "fromInclusiveIndex < maxExclusiveIndex");
    List<PlayerStatsAccumulator> fetched = content.get();
    maxExclusiveIndex = Math.min(maxExclusiveIndex, fetched.size());
    if (fromInclusiveIndex < fetched.size())
      return fetched.subList(fromInclusiveIndex, maxExclusiveIndex);
    return ImmutableList.of();
  }

  public List<PlayerStatsAccumulator> createView(int maxExclusiveIndex) {
    return createView(0, maxExclusiveIndex);
  }

}
