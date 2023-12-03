package io.github.aparx.skywarz.utils.collection;

import io.github.aparx.skywarz.utils.collection.keyed.AbstractCustomKeyedSet;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-11-27 01:29
 * @since 1.0
 */
public abstract class AbstractKeyValueSet<K, V>
    extends AbstractCustomKeyedSet<K, V>
    implements KeyValueSet<K, V> {

  public AbstractKeyValueSet() {}

  public AbstractKeyValueSet(@NonNull Map<K, V> internalMap) {
    super(internalMap);
  }

  public AbstractKeyValueSet(@NonNull KeyValueSet<K, V> initialValues) {
    super(initialValues);
  }

  public AbstractKeyValueSet(
      @NonNull KeyValueSet<K, V> initialValues,
      @NonNull IntFunction<Map<@NonNull K, @NonNull V>> mapFactory) {
    super(initialValues, mapFactory);
  }

  @Override
  public Optional<V> find(K key) {
    return Optional.ofNullable(get(key));
  }

  @Override
  public @NonNull V require(K key) {
    return find(key).orElseThrow();
  }

  @Override
  public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> factory) {
    return internalMap.computeIfPresent(key, factory);
  }

  @Override
  public V computeIfAbsent(K key, Function<? super K, ? extends V> factory) {
    return internalMap.computeIfAbsent(key, factory);
  }
}
