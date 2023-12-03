package io.github.aparx.skywarz.utils.collection;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.utils.collection.keyed.AbstractCustomKeyedSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-11-27 01:04
 * @since 1.0
 */
public class KeyedByClassSet<E> extends AbstractCustomKeyedSet<Class<? extends E>, E> {

  public KeyedByClassSet() {}

  public KeyedByClassSet(@NonNull Map<Class<? extends E>, E> internalMap) {
    super(internalMap);
    Preconditions.checkState(internalMap.isEmpty(), "Map must not contain initial entries");
  }

  @SuppressWarnings("unchecked")
  public <R extends E> Optional<R> find(@NonNull Class<R> key) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return (Optional<R>) Optional.ofNullable(internalMap.get(key));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<? extends E> getKey(E e) {
    Preconditions.checkNotNull(e, "Element must not be null");
    return (Class<? extends E>) e.getClass();
  }

  @CanIgnoreReturnValue
  public <T extends E> @NonNull T require(@NonNull Class<T> key) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return find(key).orElseThrow();
  }

  @Override
  @CanIgnoreReturnValue
  public boolean add(E e) {
    Preconditions.checkNotNull(e, "Element must not be null");
    return super.add(e);
  }

  @CanIgnoreReturnValue
  @SuppressWarnings("unchecked")
  public <R extends E> @Nullable R delete(Class<R> key) {
    return (R) internalMap.remove(key);
  }

  @SuppressWarnings("unchecked")
  public <R extends E> R computeIfAbsent(
      @NonNull Class<R> key,
      @NonNull Function<? super Class<R>, ? extends E> factory) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return (R) internalMap.computeIfAbsent(key, (type) -> {
      R value = (R) factory.apply((Class<R>) type);
      Preconditions.checkNotNull(value, "Value must not be null");
      return value;
    });
  }

  @SuppressWarnings("unchecked")
  public <R extends E> R computeIfPresent(
      @NonNull Class<R> key,
      @NonNull BiFunction<? super Class<R>, ? super R, ? extends E> factory) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return (R) internalMap.computeIfPresent(key, (k, v) -> {
      R value = (R) factory.apply((Class<R>) k, (R) v);
      Preconditions.checkNotNull(value, "Value must not be null");
      return value;
    });
  }
}
