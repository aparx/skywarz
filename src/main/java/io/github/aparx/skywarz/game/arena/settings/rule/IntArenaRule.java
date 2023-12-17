package io.github.aparx.skywarz.game.arena.settings.rule;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-17 12:22
 * @since 1.0
 */
@Getter
public class IntArenaRule extends AbstractArenaRule<Integer> {

  private @Nullable Integer fromInclusive, toExclusive;

  public IntArenaRule(@NonNull String name, Integer defaultValue) {
    this(name, defaultValue, null, null);
  }

  public IntArenaRule(@NonNull String name, Integer defaultValue, @Nullable Integer fromInclusive) {
    this(name, defaultValue, fromInclusive, null);
  }

  public IntArenaRule(@NonNull String name,
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
    if (isLowerBound() && value < fromInclusive)
      throw new IllegalArgumentException(String.format(
          "Value must be greater or equal to %s", fromInclusive));
    if (isHigherBound() && value >= toExclusive)
      throw new IllegalArgumentException(String.format(
          "Value must be less than %s", toExclusive));
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
