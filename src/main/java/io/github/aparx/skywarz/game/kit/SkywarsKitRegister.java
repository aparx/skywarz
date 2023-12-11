package io.github.aparx.skywarz.game.kit;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 20:57
 * @since 1.0
 */
public interface SkywarsKitRegister extends Iterable<@NonNull SkywarsKit> {

  void register(@NonNull SkywarsKit kit);

  Optional<SkywarsKit> findKit(@NonNull String name);

  @NonNull SkywarsKit getKit(@NonNull String name);

  boolean contains(@NonNull SkywarsKit kit);

  boolean contains(@NonNull String name);

  Stream<@NonNull SkywarsKit> stream();

}
