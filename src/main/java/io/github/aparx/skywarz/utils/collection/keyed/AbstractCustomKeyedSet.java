package io.github.aparx.skywarz.utils.collection.keyed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.IntFunction;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-11-27 01:18
 * @since 1.0
 */
public abstract class AbstractCustomKeyedSet<K, V> extends AbstractSet<V>
    implements CustomKeyedSet<K, V> {

  protected final Map<K, V> internalMap;

  public AbstractCustomKeyedSet() {
    this(Maps.newHashMap());
  }

  public AbstractCustomKeyedSet(@NonNull Map<K, V> internalMap) {
    Preconditions.checkNotNull(internalMap, "Map must not be null");
    this.internalMap = internalMap;
  }

  public AbstractCustomKeyedSet(@NonNull KeyValueSet<K, V> initialValues) {
    this(initialValues, HashMap::new);
  }

  @SuppressWarnings("unchecked")
  public AbstractCustomKeyedSet(
      @NonNull KeyValueSet<K, V> initialValues,
      @NonNull IntFunction<Map<@NonNull K, @NonNull V>> mapFactory) {
    Preconditions.checkNotNull(initialValues, "Target must not be null");
    Preconditions.checkNotNull(mapFactory, "Map factory must not be null");
    this.internalMap = mapFactory.apply(initialValues.size());
    if (initialValues instanceof AbstractCustomKeyedSet)
      internalMap.putAll(((AbstractCustomKeyedSet<K, V>) initialValues).internalMap);
    else addAll(initialValues);
  }

  @Override
  public abstract K getKey(V v);

  @Override
  public int size() {
    return internalMap.size();
  }

  @SuppressWarnings("SuspiciousMethodCalls")
  public boolean containsKey(Object key) {
    return internalMap.containsKey(key);
  }

  @Override
  public @Nullable V get(K key) {
    return internalMap.get(key);
  }

  @SuppressWarnings("SuspiciousMethodCalls")
  @CanIgnoreReturnValue
  public @Nullable Object delete(Object key) {
    return internalMap.remove(key);
  }

  @Override
  public boolean contains(Object value) {
    if (value == null) return false;
    if (super.contains(value)) return true;
    return containsKey(value);
  }

  @Override
  @CanIgnoreReturnValue
  public boolean add(V value) {
    K key = getKey(value);
    if (containsKey(key)) return false;
    internalMap.put(key, value);
    return true;
  }

  @Override
  @CanIgnoreReturnValue
  @SuppressWarnings("unchecked")
  public boolean remove(Object value) {
    if (value == null) return false;
    return internalMap.remove(getKey((V) value)) != null;
  }

  @Override
  public void clear() {
    internalMap.clear();
  }

  @Override
  public Set<K> keySet() {
    return internalMap.keySet();
  }

  @Override
  public @NonNull Iterator<V> iterator() {
    return internalMap.values().iterator();
  }
}
