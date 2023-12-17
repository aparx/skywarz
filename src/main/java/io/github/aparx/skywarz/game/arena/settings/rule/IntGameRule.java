package io.github.aparx.skywarz.game.arena.settings.rule;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-17 12:22
 * @since 1.0
 */
@Getter
public class IntGameRule extends SkywarsGameRule<Integer> {

  private @Nullable Integer fromInclusive, toExclusive;

  public IntGameRule(@NonNull String name, Integer defaultValue) {
    this(name, defaultValue, null, null);
  }

  public IntGameRule(@NonNull String name, Integer defaultValue, @Nullable Integer fromInclusive) {
    this(name, defaultValue, fromInclusive, null);
  }

  public IntGameRule(@NonNull String name,
                     Integer defaultValue,
                     @Nullable Integer fromInclusive,
                     @Nullable Integer toExclusive) {
    super(name, defaultValue);
    this.fromInclusive = fromInclusive;
    this.toExclusive = toExclusive;
    if (isRange()) {
      //noinspection DataFlowIssue
      this.fromInclusive = Math.min(fromInclusive, toExclusive);
      this.toExclusive = Math.max(fromInclusive, toExclusive);
    }
  }

  public final boolean isLowerBound() {
    return fromInclusive != null;
  }

  public final boolean isHigherBound() {
    return toExclusive != null;
  }

  public final boolean isBounded() {
    return isLowerBound() || isHigherBound();
  }

  public final boolean isRange() {
    return isLowerBound() && isHigherBound();
  }

  @Override
  public Integer validate(@Nullable Object object) {
    Preconditions.checkNotNull(object, "Object must not be null");
    int value = Integer.parseInt(object.toString());
    if (isLowerBound() && value > fromInclusive)
      throw new IndexOutOfBoundsException(value);
    if (isHigherBound() && value >= toExclusive)
      throw new IndexOutOfBoundsException(value);
    return value;
  }

  @Override
  public @Nullable List<String> getSuggestions() {
    if (!isRange()) return null;
    //noinspection DataFlowIssue
    int length = toExclusive - fromInclusive;
    if (length > 32) return null;
    List<String> list = new ArrayList<>(length);
    while (length-- >= 0)
      list.add(String.valueOf(length));
    return list;
  }
}
