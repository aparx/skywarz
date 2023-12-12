package io.github.aparx.skywarz.entity.data;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.RegisterNotifiable;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.utils.collection.KeyedByClassSet;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.function.BiFunction;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 05:04
 * @since 1.0
 */
public class SkywarsPlayerDataSet<T extends SkywarsPlayerData> extends KeyedByClassSet<T> {

  private final WeakReference<SkywarsPlayer> player;

  private static final @NonNull BiFunction<SkywarsPlayer, Class<? extends SkywarsPlayerData>, ?>
      DEFAULT_FACTORY = (player, type) -> SkywarsPlayerData.newInstance(type, player);

  public SkywarsPlayerDataSet(@NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    this.player = new WeakReference<>(player);
  }

  public @NonNull SkywarsPlayer getPlayer() {
    SkywarsPlayer player = this.player.get();
    Preconditions.checkState(player != null, "SkywarsPlayer has become invalid");
    return player;
  }

  @CanIgnoreReturnValue
  public <R extends T> @NonNull R getOrCreate(
      @NonNull Class<R> key, @NonNull BiFunction<SkywarsPlayer, Class<R>, R> factory) {
    Preconditions.checkNotNull(key, "Key must not be null");
    Preconditions.checkNotNull(factory, "Factory must not be null");
    return computeIfAbsent(key, (type) -> factory.apply(getPlayer(), type));
  }

  @CanIgnoreReturnValue
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <R extends T> @NonNull R getOrCreate(@NonNull Class<R> key) {
    return getOrCreate(key, (BiFunction<SkywarsPlayer, Class<R>, R>) (BiFunction) DEFAULT_FACTORY);
  }

  @Override
  @CanIgnoreReturnValue
  public boolean add(T value) {
    if (!super.add(value)) return false;
    if (value != null)
      value.notifyRegister();
    return true;
  }

  @Override
  @CanIgnoreReturnValue
  public boolean remove(Object value) {
    if (!super.remove(value)) return false;
    if (value != null)
      ((RegisterNotifiable) value).notifyRemoval();
    return true;
  }

  @Override
  public void clear() {
    forEach(SkywarsPlayerData::notifyRemoval);
    super.clear();
  }
}
