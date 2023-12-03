package io.github.aparx.skywarz.utils.collection;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.utils.collection.keyed.CustomKeyedSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-10-16 20:40
 * @since 1.0
 */
public interface KeyValueSet<K, V> extends CustomKeyedSet<K, V> {

  Optional<V> find(K key);

  @NonNull V require(K key);

  @CanIgnoreReturnValue
  V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> factory);

  @CanIgnoreReturnValue
  V computeIfAbsent(K key, Function<? super K, ? extends V> factory);

}
