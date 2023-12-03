package io.github.aparx.skywarz.command.tree;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 02:45
 * @since 1.0
 */
@Getter
@Setter
@Accessors(fluent = true)
public final class CommandBuilder {

  private CommandInfo.Builder infoBuilder = CommandInfo.builder();

  private CommandNodeExecutor executor;

  private CommandNode parent;

  public static CommandBuilder builder() {
    return new CommandBuilder();
  }

  public static CommandBuilder builder(
      @NonNull String name, @NonNull String @Nullable ... aliases) {
    return new CommandBuilder().name(name).aliases(aliases);
  }

  public static CommandBuilder builder(
      @Nullable CommandNode parent, @NonNull String name, @NonNull String @Nullable ... aliases) {
    return new CommandBuilder().name(name).parent(parent).aliases(aliases);
  }

  @CanIgnoreReturnValue
  public CommandBuilder name(@NonNull String name) {
    infoBuilder.name(name);
    return this;
  }

  @CanIgnoreReturnValue
  public CommandBuilder aliases(@NonNull ImmutableSet<String> aliases) {
    infoBuilder.aliases(aliases);
    return this;
  }

  @CanIgnoreReturnValue
  public CommandBuilder aliases(@NonNull String... aliases) {
    infoBuilder.aliases(aliases);
    return this;
  }

  @CanIgnoreReturnValue
  public CommandBuilder usage(String usage) {
    infoBuilder.usage(usage);
    return this;
  }

  @CanIgnoreReturnValue
  public CommandBuilder args(String args) {
    infoBuilder.args(args);
    return this;
  }

  @CanIgnoreReturnValue
  public CommandBuilder permission(String permission) {
    infoBuilder.permission(permission);
    return this;
  }

  @CanIgnoreReturnValue
  public CommandBuilder description(String description) {
    infoBuilder.description(description);
    return this;
  }

  public CommandNode build() {
    if (executor == null && StringUtils.isEmpty(infoBuilder.usage()))
      usage("<{children}>"); // default usage if no usage has been applied
    return new CommandNode(infoBuilder.build(), parent) {
      @Override
      public void execute(CommandContext context, CommandArgList args) {
        if (executor == null)
          context.setStatus(CommandContext.Status.ERROR_SYNTAX);
        else executor.execute(context, args);
      }
    };
  }


}
