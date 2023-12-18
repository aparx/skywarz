package io.github.aparx.skywarz.utils.array;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import lombok.Getter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

import java.util.*;
import java.util.function.IntFunction;

/**
 * Map-like implementation that associates objects to indices.
 * <p>This implementation utilizes a resizable array, that dynamically grows and shrinks with
 * elements added and removed.
 *
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 18:34
 * @since 1.0
 */
public class IndexMap<E> implements Iterable<IndexMap.Entry<E>>, Cloneable {

  public static final int DEFAULT_INITIAL_CAPACITY = 10;

  private static final int INDEX_NOT_FOUND = -1;

  private final int initialCapacity;

  private transient Entry<E>[] array;

  private transient int elementCount;

  public IndexMap() {
    this(DEFAULT_INITIAL_CAPACITY);
  }

  @SuppressWarnings("unchecked")
  public IndexMap(int initialCapacity) {
    this.initialCapacity = Math.max(initialCapacity, 0);
    this.array = new Entry[this.initialCapacity];
  }

  @SuppressWarnings("unchecked")
  public IndexMap(@NonNull Map<Integer, ? extends E> map) {
    this.initialCapacity = map.size();
    this.array = new Entry[initialCapacity];
    putAll(map);
  }

  @SuppressWarnings({"unchecked"})
  public IndexMap(@NonNull IndexMap<E> other) {
    this.initialCapacity = other.capacity();
    this.array = new Entry[initialCapacity];
    this.elementCount = other.elementCount;
    int elemTracker = 0;
    // copy keys & values
    for (int i = 0, len = other.capacity(); i < len; ++i) {
      Entry<E> otherEntry = other.array[i];
      if (otherEntry != null) {
        array[i] = new Entry<>(otherEntry.getIndex(), otherEntry.getObject());
        ++elemTracker;
      }
    }
    if (elemTracker != elementCount)
      throw new ConcurrentModificationException();
  }

  @Pure
  public final @NonNegative int capacity() {
    return array.length;
  }

  @Pure
  public final @NonNegative int size() {
    return elementCount;
  }

  public void clear() {
    int previousCapacity = capacity();
    resizeToCapacity0(initialCapacity, false);
    if (capacity() == previousCapacity)
      Arrays.fill(array, null);
    elementCount = 0;
  }

  public void ensureCapacity(int capacity) {
    if (capacity > capacity())
      resizeToCapacity0(capacity, true);
  }

  public @Nullable E get(int index) {
    Preconditions.checkElementIndex(index, capacity());
    Entry<E> entry = array[index];
    if (entry != null)
      return entry.getValue();
    return null;
  }

  @CanIgnoreReturnValue
  public @Nullable E put(@NonNull Entry<E> entry) {
    Preconditions.checkNotNull(entry, "Entry must not be null");
    return put(entry.getIndex(), entry.getValue());
  }

  @CanIgnoreReturnValue
  public @Nullable E put(int index, E value) {
    Preconditions.checkState(index >= 0, "Index must be positive");
    if (index >= capacity())
      resizeToCapacity0(calculateNewCapacity(1 + index), true);
    Entry<E> entry = array[index];
    E previousValue = null;
    if (entry == null) {
      array[index] = new Entry<>(index, value);
      ++elementCount;
    } else {
      previousValue = entry.getValue();
      entry.setValue(value);
    }
    return previousValue;
  }

  public void putAll(@NonNull Map<@NonNull Integer, ? extends E> map, int indexOffset) {
    Preconditions.checkArgument(indexOffset >= 0, "indexOffset must be zero or positive");
    Preconditions.checkNotNull(map, "Map must not be null");
    ensureCapacity(indexOffset + map.size());
    for (Map.Entry<Integer, ? extends E> entry : map.entrySet())
      put(indexOffset + Objects.requireNonNull(entry.getKey()), entry.getValue());
  }

  public void putAll(@NonNull Map<@NonNull Integer, ? extends E> map) {
    putAll(map, 0);
  }

  public void putAll(E @NonNull [] array, int indexOffset) {
    Preconditions.checkArgument(indexOffset >= 0, "indexOffset must be zero or positive");
    ensureCapacity(indexOffset + array.length);
    for (int i = 0, len = array.length; i < len; ++i)
      put(indexOffset + i, array[i]);
  }

  public void putAll(E @NonNull [] array) {
    putAll(array, 0);
  }

  public void putAll(@NonNull Iterable<? extends E> iterable, int indexOffset) {
    Preconditions.checkArgument(indexOffset >= 0, "indexOffset must be zero or positive");
    if (iterable instanceof Collection)
      ensureCapacity(indexOffset + ((Collection<?>) iterable).size());
    Iterator<? extends E> iterator = iterable.iterator();
    for (int i = 0; iterator.hasNext(); ++i)
      put(indexOffset + i, iterator.next());
  }

  public void putAll(@NonNull Iterable<? extends E> iterable) {
    putAll(iterable, 0);
  }

  @CanIgnoreReturnValue
  public @Nullable E remove(int index) {
    Preconditions.checkElementIndex(index, size());
    E previousValue = null;
    Entry<E> entry = array[index];
    if (entry != null) {
      previousValue = entry.getValue();
      entry.setValue(null);
    }
    array[index] = null;
    --elementCount;
    return previousValue;
  }

  @CanIgnoreReturnValue
  public boolean remove(int index, Object value) {
    Preconditions.checkElementIndex(index, size());
    Entry<E> entry = array[index];
    if (entry != null && Objects.equals(entry.getValue(), value))
      return remove(index) != null;
    return false;
  }

  public boolean containsKey(int index) {
    return index >= 0 && index < capacity() && array[index] != null;
  }

  public boolean containsValue(Object value) {
    return indexOf(value) != INDEX_NOT_FOUND;
  }

  public boolean contains(int index, Object value) {
    if (index < 0 || index >= capacity())
      return false;
    Entry<E> entry = array[index];
    return entry != null && Objects.equals(entry.getValue(), value);
  }

  @CheckReturnValue
  public int indexOf(Object value) {
    if (value == null) {
      for (int i = 0, len = capacity(); i < len; ++i) {
        Entry<E> entry = array[i];
        if (entry != null && entry.object == null)
          return i;
      }
    } else {
      for (int i = 0, len = capacity(); i < len; ++i) {
        Entry<E> entry = array[i];
        if (entry != null && Objects.equals(value, entry.getValue()))
          return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  @CheckReturnValue
  public int lastIndexOf(Object value) {
    if (value == null) {
      for (int i = capacity(); i > 0; --i) {
        Entry<E> entry = array[i - 1];
        if (entry != null && entry.object == null)
          return i - 1;
      }
    } else {
      for (int i = capacity(); i > 0; --i) {
        Entry<E> entry = array[i - 1];
        if (entry != null && Objects.equals(value, entry.getValue()))
          return i - 1;
      }
    }
    return INDEX_NOT_FOUND;
  }

  public Map<Integer, E> toMap() {
    return toMap(HashMap::new);
  }

  public Map<Integer, E> toMap(@NonNull IntFunction<? extends Map<Integer, E>> factory) {
    Map<Integer, E> map = factory.apply(size());
    for (int i = 0; i < array.length; ++i) {
      Entry<E> entry = array[i];
      if (entry != null)
        map.put(i, entry.getValue());
    }
    return map;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void resizeToCapacity0(int newCapacity, boolean copyThisArray) {
    int oldCapacity = capacity();
    if (oldCapacity == newCapacity)
      return;
    Entry[] newArray = new Entry[newCapacity];
    if (copyThisArray && array.length != 0 && newCapacity != 0)
      System.arraycopy(array, 0, newArray, 0, Math.min(array.length, newCapacity));
    this.array = newArray;
  }

  private int calculateNewCapacity(int newCapacity) {
    return (int) Math.ceil(newCapacity * 1.5);
  }

  @Override
  public @NonNull Iterator<Entry<E>> iterator() {
    return new Iterator<>() {

      int cursor = 0;
      Boolean hasNext;

      @Override
      public boolean hasNext() {
        if (hasNext != null)
          return hasNext;
        for (int i = cursor; i < array.length; ++i)
          if (array[i] != null)
            return hasNext = Boolean.TRUE;
        return hasNext = Boolean.FALSE;
      }

      @Override
      public Entry<E> next() {
        hasNext = null;
        while (cursor < array.length) {
          int i = cursor++;
          Entry<E> entry = array[i];
          if (entry != null)
            return entry;
        }
        throw new NoSuchElementException();
      }
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  public IndexMap<E> clone() {
    try {
      IndexMap<E> clone = (IndexMap<E>) super.clone();
      clone.array = array.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Getter
  public static final class Entry<E> {
    private final int index;
    private Object object;

    private Entry(int index, Object object) {
      this.index = index;
      this.object = object;
    }

    public static <E> Entry<E> of(int index, E value) {
      Preconditions.checkArgument(index >= 0, "Index must be positive");
      return new Entry<>(index, value);
    }

    public void setValue(E value) {
      this.object = value;
    }

    @SuppressWarnings("unchecked") // OK
    public E getValue() {
      return (E) object;
    }
  }

}
