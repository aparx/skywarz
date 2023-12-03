package io.github.aparx.skywarz.command;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.exceptions.CommandError;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 02:59
 * @since 1.0
 */
@Getter
public class CommandContext {

  private final @NonNull Command command;
  private final @NonNull CommandSender sender;
  private final @NonNull CommandArgList initialArgs;
  private final @NonNull String label;

  private @NonNull Status status = Status.OK;

  public CommandContext(
      @NonNull Command command,
      @NonNull CommandSender sender,
      @NonNull CommandArgList initialArgs,
      @NonNull String label) {
    Preconditions.checkNotNull(sender, "Sender must not be null");
    Preconditions.checkNotNull(command, "Command must not be null");
    Preconditions.checkNotNull(initialArgs, "Args must not be null");
    Preconditions.checkNotNull(label, "Label must not be null");
    this.sender = sender;
    this.command = command;
    this.initialArgs = initialArgs;
    this.label = label;
  }

  public void setStatus(@NonNull Status status) {
    Preconditions.checkNotNull(status, "Status must not be null");
    this.status = status;
  }

  public boolean isStatus(Status status) {
    return this.status == status;
  }

  public boolean isError() {
    return status != Status.OK;
  }

  public boolean isPlayer() {
    return sender instanceof Player;
  }

  public @NonNull Player getPlayer() {
    if (!(sender instanceof Player))
      throw new CommandError((t, lang) -> lang.substitute(lang.getErrorSelfNotPlayer()));
    return (Player) sender;
  }

  public enum Status {
    OK,
    ERROR,
    ERROR_PERMISSION,
    ERROR_SYNTAX
  }

}
