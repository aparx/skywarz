package io.github.aparx.skywarz.game.arena.settings.rule;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-17 12:22
 * @since 1.0
 */
@Getter
public class BooleanGameRule extends SkywarsGameRule<Boolean> {

  private final List<String> suggestions = List.of("true", "false");

  public BooleanGameRule(@NonNull String name, boolean defaultValue) {
    super(name, defaultValue);
  }

  @Override
  public Boolean validate(Object object) {
    return Boolean.parseBoolean(Objects.toString(object, null));
  }

  @Override
  public @Nullable List<String> getSuggestions() {
    return suggestions;
  }

  @Override
  public @NonNull Boolean getDefaultValue() {
    return super.getDefaultValue();
  }
}
