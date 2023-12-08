package io.github.aparx.skywarz.language;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 10:24
 * @since 1.0
 */
public interface MessageStorage<K> {

  Optional<LocalizedMessage> find(@NonNull K key);

  @NonNull LocalizedMessage get(@NonNull K key);

  @CanIgnoreReturnValue
  @Nullable LocalizedMessage store(@NonNull K key, @NonNull LocalizedMessage message);

  @CanIgnoreReturnValue
  @Nullable LocalizedMessage store(@NonNull K key, String message);

  @CanIgnoreReturnValue
  @Nullable LocalizedMessage store(@NonNull K key, Object message);

  @CanIgnoreReturnValue
  default @Nullable LocalizedMessage store(@NonNull K key, @NonNull Collection<?> messages) {
    return store(key, messages.stream().map(String::valueOf).collect(Collectors.joining("\n")));
  }

}
