package io.github.aparx.skywarz.database.object;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-10 08:46
 * @since 1.0
 */
public interface FetchableLazyObject<T> {

  @Nullable T get();

  CompletableFuture<? extends T> fetch();

  @NonNull FetchableObjectState getState();

}
