package io.github.aparx.skywarz.database.object;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-10 09:47
 * @since 1.0
 */
public final class CachableLazyObjectRegister<K, T> {

  private final Map<K, CachableLazyObject<T>> map = new ConcurrentHashMap<>();

  private final @NonNull TickDuration purgeInterval;

  private final @NonNull Function<K, CachableLazyObject<T>> factory;

  private BukkitTask task;

  public CachableLazyObjectRegister(
      @NonNull TickDuration purgeInterval,
      @NonNull Function<K, CachableLazyObject<T>> factory) {
    Preconditions.checkNotNull(purgeInterval, "Interval must not be null");
    Preconditions.checkNotNull(factory, "Factory must not be null");
    this.purgeInterval = purgeInterval;
    this.factory = factory;
  }

  public void clear() {
    map.clear();
  }

  public void register() {
    if (task != null) task.cancel();
    long interval = purgeInterval.toTicks();
    task = Bukkit.getScheduler().runTaskTimer(Skywars.plugin(), () -> clear(), interval, interval);
  }

  public void unregister() {
    if (task != null) {
      task.cancel();
      task = null;
    }
  }

  @CheckReturnValue
  public Optional<CachableLazyObject<T>> find(@NonNull K key) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return Optional.ofNullable(map.get(key));
  }

  @CanIgnoreReturnValue
  public CachableLazyObject<T> get(@NonNull K key) {
    return find(key).orElseThrow();
  }

  @CanIgnoreReturnValue
  public @NonNull CachableLazyObject<T> getOrCreate(@NonNull K key) {
    return Objects.requireNonNull(map.computeIfAbsent(key, factory));
  }

  public boolean contains(@NonNull K key) {
    return map.containsKey(key);
  }

  @CanIgnoreReturnValue
  public @Nullable CachableLazyObject<T> remove(@NonNull K key) {
    return map.remove(key);
  }

}
