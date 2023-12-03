package io.github.aparx.skywarz.command.tree;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.arguments.CommandArgList;

import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 02:50
 * @since 1.0
 */
public interface CommandNodeExecutor {

  void execute(CommandContext context, CommandArgList args);

  default List<String> onTabComplete(CommandContext context, CommandArgList args) {
    return null;
  }

}
