package io.github.aparx.skywarz.utils.collection.keyed;


import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-11-27 01:13
 * @since 1.0
 */
public interface CustomKeyedSet<K, V> extends Set<V> {

  @Nullable V get(K key);

  boolean containsKey(Object key);

  K getKey(V v);

  Set<K> keySet();

  default Map<K, V> toMap() {
    Map<K, V> target = new HashMap<>(size());
    for (K key : keySet())
      target.put(key, get(key));
    return target;
  }

}