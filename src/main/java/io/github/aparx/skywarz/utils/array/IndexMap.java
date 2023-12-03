package io.github.aparx.skywarz.utils.array;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 18:34
 * @since 1.0
 */
public class IndexMap<V> {

  private static final int DEFAULT_INITIAL_CAPACITY = 10;

  private Object[] array;

  private int elementCount;

  public IndexMap() {
    this.array = new Object[DEFAULT_INITIAL_CAPACITY];
  }

  public IndexMap(int initialCapacity) {
    this.array = new Object[Math.max(initialCapacity, 0)];
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
  public @Nullable V get(@NonNegative int index) {
    Preconditions.checkElementIndex(index, size());
    return (V) array[index];
  }

  public Optional<V> find(@NonNegative int index) {
    if (index >= elementCount) return Optional.empty();
    return Optional.ofNullable(get(index));
  }

  public @NonNull V require(@NonNegative int index) {
    return find(index).orElseThrow();
  }

  @CanIgnoreReturnValue
  @SuppressWarnings("unchecked")
  public @Nullable V put(int index, @NonNull V value) {
    Preconditions.checkState(index >= 0, "Index must be positive");
    Preconditions.checkNotNull(value, "Value must not be null");
    regrowIfNeeded(index);
    Object previousValue = array[index];
    array[index] = value;
    if (previousValue == null)
      ++elementCount;
    return (V) previousValue;
  }

  @CanIgnoreReturnValue
  @SuppressWarnings("unchecked")
  public @Nullable V remove(int index) {
    Preconditions.checkElementIndex(index, size());
    Object previousValue = array[index];
    array[index] = null;
    --elementCount;
    return (V) previousValue;
  }

  @CanIgnoreReturnValue
  public boolean remove(int index, Object value) {
    Preconditions.checkElementIndex(index, size());
    if (Objects.equals(array[index], value))
      return remove(index) != null;
    return false;
  }

  /** Regrow array to accommodate for given {@code index} */
  private void regrowIfNeeded(int index) {
    final int cap = capacity();
    int grow = 1 + index - cap;
    if (grow > 0)
      resizeToCapacity(cap + (int) Math.ceil(grow * 1.75));
  }

  private void resizeToCapacity(int newCapacity) {
    Object[] newArray = new Object[newCapacity];
    System.arraycopy(array, 0, newArray, 0, Math.min(array.length, newCapacity));
    this.array = newArray;
  }


}
