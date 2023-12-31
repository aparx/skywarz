package io.github.aparx.skywarz.utils.collection;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.RegisterNotifiable;
import io.github.aparx.skywarz.utils.Snowflake;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.ArrayUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-11-27 00:41
 * @since 1.0
 */
@UtilityClass
public final class KeyValueSets {

  public static <K, V> KeyValueSet<K, V> of(@NonNull Function<V, K> keyMapper) {
    Preconditions.checkNotNull(keyMapper, "Mapper must not be null");
    return new AbstractKeyValueSet<>() {
      @Override
      public K getKey(V v) {
        return keyMapper.apply(v);
      }
    };
  }

  @SafeVarargs
  public static <K, E> KeyValueSet<K, E> of(@NonNull Function<E, K> keyMapper, E... elements) {
    KeyValueSet<K, E> valueSet = KeyValueSets.of(keyMapper);
    if (ArrayUtils.getLength(elements) == 0)
      return valueSet;
    valueSet.addAll(Arrays.asList(elements));
    return valueSet;
  }

  public static <K, V extends Snowflake<? extends K>> KeyValueSet<K, V> ofSnowflake() {
    return of((value) -> Preconditions.checkNotNull(value.getId()));
  }

  public static <K, V extends RegisterNotifiable>
  KeyValueSet<K, V> ofNotifable(@NonNull Function<V, K> keyMapper) {
    Preconditions.checkNotNull(keyMapper, "Mapper must not be null");
    return new AbstractKeyValueSet<>() {
      @Override
      public K getKey(V v) {
        return keyMapper.apply(v);
      }

      @Override
      public boolean add(V value) {
        if (!super.add(value)) return false;
        if (value != null)
          value.notifyRegister();
        return true;
      }

      @Override
      public boolean remove(Object value) {
        if (!super.remove(value)) return false;
        if (value != null)
          ((RegisterNotifiable) value).notifyRemoval();
        return true;
      }
    };
  }

  public static <K, V extends RegisterNotifiable & Snowflake<K>>
  KeyValueSet<K, V> ofNotifable() {
    return ofNotifable(Snowflake::getId);
  }


  public static <K, E> KeyValueSet<K, E> copyOf(
      @NonNull KeyValueSet<K, E> source,
      @NonNull Function<E, K> keyMapper,
      @NonNull IntFunction<Map<@NonNull K, @NonNull E>> mapFactory) {
    Preconditions.checkNotNull(source, "Set must not be null");
    Preconditions.checkNotNull(keyMapper, "Mapper must not be null");
    return new AbstractKeyValueSet<>(source, mapFactory) {
      @Override
      public K getKey(E e) {
        return keyMapper.apply(e);
      }
    };
  }

  public static <K, E> KeyValueSet<K, E> copyOf(
      @NonNull KeyValueSet<K, E> source,
      @NonNull Function<E, K> keyMapper) {
    return copyOf(source, keyMapper, HashMap::new);
  }

  public static <K, E> KeyValueSet<K, E> copyOf(
      @NonNull KeyValueSet<K, E> source,
      @NonNull IntFunction<Map<@NonNull K, @NonNull E>> mapFactory) {
    return copyOf(source, source::getKey, mapFactory);
  }

  public static <K, E> KeyValueSet<K, E> copyOf(@NonNull KeyValueSet<K, E> source) {
    return copyOf(source, source::getKey);
  }

  public static <E extends @NonNull Object> KeyedByClassSet<E> ofClass() {
    return new KeyedByClassSet<>();
  }

  public static <E extends @NonNull Object> KeyedByClassSet<E> ofClass(
      @NonNull Map<Class<? extends E>, E> internalMap) {
    return new KeyedByClassSet<>(internalMap);
  }

}
