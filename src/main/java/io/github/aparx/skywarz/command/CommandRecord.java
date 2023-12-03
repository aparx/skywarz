package io.github.aparx.skywarz.command;

import io.github.aparx.skywarz.command.arguments.CommandArgList;
import lombok.Data;
import lombok.NonNull;
import org.bukkit.command.Command;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 02:38
 * @since 1.0
 */
@Data
public final class CommandRecord {
  final @NonNull Command command;
  final @NonNull CommandArgList args;
}
