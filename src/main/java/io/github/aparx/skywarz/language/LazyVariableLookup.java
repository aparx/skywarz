package io.github.aparx.skywarz.language;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.bufig.ArrayPath;
import lombok.Getter;
import org.apache.commons.text.lookup.StringLookup;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-08 12:34
 * @since 1.0
 */
@Getter
public class LazyVariableLookup implements StringLookup {

  private final @NonNull Map<String, Object> dataMap;

  public LazyVariableLookup() {
    this(new HashMap<>());
  }

  private LazyVariableLookup(@NonNull Map<String, Object> objectMap) {
    Preconditions.checkNotNull(objectMap, "Map must not be null");
    this.dataMap = objectMap;
  }

  @Override
  public String lookup(String key) {
    if (!dataMap.containsKey(key))
      return null;
    Object object = dataMap.get(key);
    if (object == null)
      return null;
    if (object instanceof Supplier)
      return Objects.toString(((Supplier<?>) object).get(), null);
    return Objects.toString(object, null);
  }

  @CanIgnoreReturnValue
  public @NonNull LazyVariableLookup set(ArrayPath key, Object object) {
    dataMap.put(key.join(), object);
    return this;
  }

  @CanIgnoreReturnValue
  public @NonNull LazyVariableLookup setIfAbsent(ArrayPath key, Object object) {
    dataMap.putIfAbsent(key.join(), object);
    return this;
  }

  @CanIgnoreReturnValue
  public @NonNull LazyVariableLookup set(ArrayPath key, Supplier<?> supplier) {
    dataMap.put(key.join(), supplier);
    return this;
  }

  @CanIgnoreReturnValue
  public @NonNull LazyVariableLookup set(String key, Supplier<?> supplier) {
    return set(ArrayPath.parse(key), supplier);
  }
}
