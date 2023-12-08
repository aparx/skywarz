package io.github.aparx.skywarz.game.kit;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 20:57
 * @since 1.0
 */
public interface KitRegister extends Iterable<@NonNull Kit> {

  void register(@NonNull Kit kit);

  Optional<Kit> findKit(@NonNull String name);

  @NonNull Kit getKit(@NonNull String name);

  boolean contains(@NonNull Kit kit);

  boolean contains(@NonNull String name);

  Stream<@NonNull Kit> stream();

}
