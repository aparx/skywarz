package io.github.aparx.skywarz.command;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 02:28
 * @since 1.0
 */
@With
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandInfo {

  final @NonNull String name;
  final @NonNull ImmutableSet<String> aliases;
  final @Nullable String description;
  final @Nullable SkywarsPermission permission;
  final @Nullable String usage;

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(@NonNull String name) {
    return new Builder(name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CommandInfo that = (CommandInfo) o;
    return Objects.equals(name, that.name)
        && Objects.equals(aliases, that.aliases)
        && Objects.equals(description, that.description)
        && Objects.equals(permission, that.permission)
        && Objects.equals(usage, that.usage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, aliases, description, permission, usage);
  }

  @Getter
  @Setter
  @Accessors(fluent = true)
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Builder {

    private static final String DEFAULT_USAGE = "/{label} {args}";

    private @NonNull String name;
    private ImmutableSet<String> aliases = ImmutableSet.of();
    private @Nullable String description;
    private @Nullable SkywarsPermission permission;
    private String usage = DEFAULT_USAGE;

    @CanIgnoreReturnValue
    public Builder aliases(@NonNull ImmutableSet<String> aliases) {
      Preconditions.checkNotNull(aliases, "Aliases must not be null");
      Validate.noNullElements(aliases, "An alias is null, but must not be");
      this.aliases = aliases;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder aliases(String... aliases) {
      return aliases(aliases != null
          ? ImmutableSet.copyOf(aliases)
          : ImmutableSet.of());
    }

    @CanIgnoreReturnValue
    public Builder args(String argsUsage) {
      return usage(DEFAULT_USAGE + ' ' + argsUsage);
    }

    @CanIgnoreReturnValue
    public Builder name(String name) {
      Preconditions.checkNotNull(name, "Name must not be null");
      Preconditions.checkState(!name.isBlank(), "Name must not be blank");
      this.name = name;
      return this;
    }

    @CheckReturnValue
    public CommandInfo build() {
      Preconditions.checkNotNull(name, "Name has not been specified");
      return new CommandInfo(name, aliases, description, permission, usage);
    }
  }

}
