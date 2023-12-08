package io.github.aparx.skywarz.utils.array;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 18:34
 * @since 1.0
 */
public class IndexMap<E> implements Iterable<E> {

  private static final int DEFAULT_INITIAL_CAPACITY = 10;

  private Object[] array;

  private int elementCount;

  public IndexMap() {
    this.array = new Object[DEFAULT_INITIAL_CAPACITY];
  }

  public IndexMap(int initialCapacity) {
    this.array = new Object[Math.max(initialCapacity, 0)];
  }

  public IndexMap(@NonNull Map<Integer, ? extends E> map) {
    this.array = new Object[map.size()];
    putAll(map);
  }

  public IndexMap(@NonNull IndexMap<? extends E> other) {
    this.array = new Object[other.capacity()];
    this.elementCount = other.elementCount;
    System.arraycopy(other.array, 0, array, 0, other.capacity());
  }

  public int capacity() {
    return array.length;
  }

  public int size() {
    return elementCount;
  }

  public void clear() {
    Arrays.fill(array, null);
    elementCount = 0;
  }

  public void ensureCapacity(int capacity) {
    if (capacity > capacity())
      resizeToCapacity(capacity);
  }

  @SuppressWarnings("unchecked")
  public @Nullable E get(@NonNegative int index) {
    Preconditions.checkElementIndex(index, size());
    return (E) array[index];
  }

  public Optional<E> find(@NonNegative int index) {
    if (index >= elementCount) return Optional.empty();
    return Optional.ofNullable(get(index));
  }

  public @NonNull E require(@NonNegative int index) {
    return find(index).orElseThrow();
  }

  @CanIgnoreReturnValue
  @SuppressWarnings("unchecked")
  public @Nullable E put(int index, @NonNull E value) {
    Preconditions.checkState(index >= 0, "Index must be positive");
    Preconditions.checkNotNull(value, "Value must not be null");
    regrowIfNeeded(index);
    Object previousValue = array[index];
    array[index] = value;
    if (previousValue == null)
      ++elementCount;
    return (E) previousValue;
  }

  public void putAll(Map<Integer, ? extends E> map) {
    Preconditions.checkNotNull(map, "Map must not be null");
    map.forEach((index, obj) -> array[index] = obj);
  }

  @CanIgnoreReturnValue
  @SuppressWarnings("unchecked")
  public @Nullable E remove(int index) {
    Preconditions.checkElementIndex(index, size());
    Object previousValue = array[index];
    array[index] = null;
    --elementCount;
    return (E) previousValue;
  }

  @CanIgnoreReturnValue
  public boolean remove(int index, Object value) {
    Preconditions.checkElementIndex(index, size());
    if (Objects.equals(array[index], value))
      return remove(index) != null;
    return false;
  }

  public Object[] toArray() {
    return ArrayUtils.clone(array);
  }

  @SuppressWarnings({"SuspiciousSystemArraycopy", "unchecked"})
  public E[] toArray(E[] e) {
    if (e.length == 0)
      e = (E[]) ArrayUtils.newInstance(e.getClass().getComponentType(), this.array.length);
    System.arraycopy(this.array, 0, e, 0, Math.min(this.array.length, e.length));
    return e;
  }

  public Map<Integer, Object> toMap() {
    return toMap(HashMap::new);
  }

  public Map<Integer, Object> toMap(@NonNull IntFunction<? extends Map<Integer, Object>> factory) {
    Map<Integer, Object> map = factory.apply(size());
    for (int i = 0; i < array.length; ++i)
      if (array[i] != null)
        map.put(i, array[i]);
    return map;
  }

  /** Regrow array to accommodate for given {@code index} */
  private void regrowIfNeeded(int index) {
    final int cap = capacity();
    int grow = 1 + index - cap;
    if (grow > 0)
      resizeToCapacity(cap + (int) Math.ceil(grow * 1.5));
  }

  private void resizeToCapacity(int newCapacity) {
    Object[] newArray = new Object[newCapacity];
    System.arraycopy(array, 0, newArray, 0, Math.min(array.length, newCapacity));
    this.array = newArray;
  }


  @SuppressWarnings("unchecked")
  public void forEach(ObjIntConsumer<E> consumer) {
    for (int i = 0; i < array.length; ++i)
      if (array[i] != null)
        consumer.accept((E) array[i], i);
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {

      int cursor = 0;

      @Override
      public boolean hasNext() {
        for (int i = cursor; i < array.length; ++i)
          if (array[i] != null)
            return true;
        return false;
      }

      @SuppressWarnings("unchecked")
      @Override
      public E next() {
        while (cursor < array.length) {
          int i = cursor++;
          if (array[i] != null)
            return (E) array[i];
        }
        throw new ConcurrentModificationException();
      }
    };
  }
}
