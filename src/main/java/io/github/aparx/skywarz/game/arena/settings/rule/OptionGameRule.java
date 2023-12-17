package io.github.aparx.skywarz.game.arena.settings.rule;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-17 12:22
 * @since 1.0
 */
public class OptionGameRule extends SkywarsGameRule<String> {

  private final Set<String> options;

  public OptionGameRule(@NonNull String name, String defaultValue, Collection<String> options) {
    super(name, defaultValue);
    this.options = new LinkedHashSet<>(options);
  }

  @Override
  public String validate(@Nullable Object object) {
    String value = Objects.toString(object, null);
    if (!options.contains(value))
      throw new IllegalArgumentException("Not an option");
    return value;
  }

  @Override
  public @Nullable List<String> getSuggestions() {
    return new LinkedList<>(options);
  }
}
