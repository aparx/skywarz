package io.github.aparx.skywarz.game.arena.settings.rule;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-17 12:21
 * @since 1.0
 */
@Getter
public abstract class SkywarsGameRule<T> {

  private final @NonNull String name;
  private final T defaultValue;

  public SkywarsGameRule(@NonNull String name, T defaultValue) {
    Preconditions.checkNotNull(name, "Name must not be null");
    this.name = name;
    this.defaultValue = defaultValue;
  }

  public abstract T validate(Object object);

  // TODO filter after argument
  public abstract @Nullable List<String> getSuggestions();
}
