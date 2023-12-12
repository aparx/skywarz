package io.github.aparx.skywarz.game.kit;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 20:57
 * @since 1.0
 */
public interface GameKitRegister extends Iterable<@NonNull GameKit> {

  void register(@NonNull GameKit kit);

  Optional<GameKit> findKit(@NonNull String name);

  @NonNull GameKit getKit(@NonNull String name);

  boolean contains(@NonNull GameKit kit);

  boolean contains(@NonNull String name);

  Stream<@NonNull GameKit> stream();

}
