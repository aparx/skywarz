package io.github.aparx.skywarz.game.arena.settings.rule;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.github.aparx.bufig.utils.ConversionUtils;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-17 12:22
 * @since 1.0
 */
@Getter
public class EnumArenaRule<E extends Enum<E>> extends AbstractArenaRule<E> {

  private final @NonNull Class<E> type;

  private final List<String> suggestions;

  public EnumArenaRule(@NonNull String name, E defaultValue, @NonNull Class<E> type) {
    super(name, defaultValue);
    Preconditions.checkNotNull(type, "Type must not be null");
    this.type = type;
    this.suggestions = Arrays.stream(type.getEnumConstants())
        .map(Enum::name)
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  public E validate(Object object) {
    Preconditions.checkNotNull(object, "Object must not be null");
    return ConversionUtils.toEnum(object, type);
  }

}
