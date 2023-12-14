package io.github.aparx.skywarz.database.object;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class CachableLazyObject<T> implements FetchableLazyObject<T> {

  /** Milliseconds, for how long the cached object is considered fresh */
  private final long freshTime;

  private final @NonNull Supplier<@Nullable CompletableFuture<? extends T>> fetch;
  private final @Nullable T fallback;

  @Getter
  private volatile @Nullable T cached;

  private volatile long lastCacheTime;

  private volatile FetchableObjectState state = FetchableObjectState.LOADING;

  public static <T> CachableLazyObject<T> of(
      @NonNull Duration freshDuration,
      @NonNull Supplier<CompletableFuture<? extends T>> fetch,
      @Nullable T fallback) {
    Preconditions.checkNotNull(freshDuration, "Duration must not be null");
    Preconditions.checkNotNull(fetch, "Fetch supplier must not be null");
    return new CachableLazyObject<>(freshDuration.toMillis(), fetch, fallback);
  }

  public static <T> CachableLazyObject<T> of(
      @NonNull Duration freshDuration,
      @NonNull Supplier<CompletableFuture<? extends T>> fetch) {
    return of(freshDuration, fetch, null);
  }

  @Override
  public T get() {
    fetch();
    if (cached == null && state != FetchableObjectState.FRESH)
      return fallback;
    return cached;
  }

  @Synchronized
  public @NonNull FetchableObjectState getState() {
    updateState();
    return state;
  }

  @Override
  @Synchronized
  @CanIgnoreReturnValue
  public CompletableFuture<? extends T> fetch() {
    if (getState() == FetchableObjectState.FRESH)
      return CompletableFuture.completedFuture(cached);
    // STALE: thus force refresh
    lastCacheTime = System.currentTimeMillis();
    CompletableFuture<? extends T> future = fetch.get();
    if (future == null) {
      // FRESH: due to the database being deactivated
      state = FetchableObjectState.FRESH;
      return CompletableFuture.completedFuture(fallback);
    }
    state = FetchableObjectState.LOADING;
    return future.thenApply((objectFetched) -> {
      // FRESH: due to the fetch finishing
      lastCacheTime = System.currentTimeMillis();
      state = FetchableObjectState.FRESH;
      return cached = objectFetched;
    });
  }

  /** Purges the cache. */
  @Synchronized
  public void purge() {
    lastCacheTime = 0;
    state = FetchableObjectState.STALE;
  }

  private void updateState() {
    if (System.currentTimeMillis() - lastCacheTime > freshTime)
      state = FetchableObjectState.STALE;
  }

}