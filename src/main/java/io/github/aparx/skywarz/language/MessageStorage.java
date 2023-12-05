package io.github.aparx.skywarz.language;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

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

}
