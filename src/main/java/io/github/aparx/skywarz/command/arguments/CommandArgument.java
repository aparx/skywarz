package io.github.aparx.skywarz.command.arguments;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import io.github.aparx.skywarz.language.LocalizableError;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.language.MessageKeys;
import lombok.AccessLevel;
import lombok.Getter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 06:17
 * @since 1.0
 */
@Getter
public final class CommandArgument {

  private final @NonNull CommandArgList context;
  private final @NonNegative int index;

  @Getter(AccessLevel.NONE)
  private final @NonNull String argument;

  public CommandArgument(
      @NonNull CommandArgList context,
      @NonNegative int index,
      @NonNull String argument) {
    Preconditions.checkNotNull(context, "Context must not be null");
    Preconditions.checkNotNull(argument, "Argument must not be null");
    this.context = context;
    this.index = index;
    this.argument = argument;
  }

  public boolean isMatching(String name) {
    return argument.equalsIgnoreCase(name);
  }

  public boolean isBlank() {
    return argument.isBlank();
  }

  public boolean isEmpty() {
    return argument.isEmpty();
  }

  public int length() {
    return argument.length();
  }

  public String get() {
    return argument;
  }

  public int getInt(int defaultValue) {
    return Optional.ofNullable(Ints.tryParse(argument)).orElse(defaultValue);
  }

  public boolean isInt() {
    return Ints.tryParse(argument) != null;
  }

  public int getInt() {
    return LocalizableError.localizeThrow(
        () -> Preconditions.checkNotNull(Ints.tryParse(argument)),
        (lang) -> lang.substitute(MessageKeys.Errors.INTEGER, argument));
  }

  public double getDouble(double defaultValue) {
    return Optional.ofNullable(Doubles.tryParse(argument)).orElse(defaultValue);
  }

  public double getDouble() {
    return LocalizableError.localizeThrow(
        () -> Preconditions.checkNotNull(Doubles.tryParse(argument)),
        (lang) -> lang.substitute(MessageKeys.Errors.NUMBER, argument));
  }

  public TeamEnum getTeam(TeamEnum defaultValue) {
    return getTeam0(argument, defaultValue);
  }

  public TeamEnum getTeam() {
    return Optional.ofNullable(getTeam0(argument, null)).orElseThrow(
        () -> new IllegalArgumentException(String.format("Argument %s not a team", argument)));
  }

  private TeamEnum getTeam0(String name, TeamEnum defaultValue) {
    try {
      return TeamEnum.valueOf(name.toUpperCase());
    } catch (Exception e) {
      return defaultValue;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CommandArgument that = (CommandArgument) o;
    return index == that.index
        && Objects.equals(context, that.context)
        && Objects.equals(argument, that.argument);
  }

  @Override
  public int hashCode() {
    return Objects.hash(context, index, argument);
  }
}
