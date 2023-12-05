package io.github.aparx.skywarz.command.commands.arena.update;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaSpawnCommand;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.language.Language;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 06:27
 * @since 1.0
 */
public class ArenaSetLobbyCommand extends AbstractArenaSpawnCommand {

  public ArenaSetLobbyCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
            .name("lobby")
            .args("<Arena>")
            .description("Update the lobby spawn to the current location")
            .build(),
        parent);
  }

  @Override
  protected void setLocation(
      Location location, Arena arena, CommandContext context, CommandArgList args) {
    arena.getData().setLobby(location);
    context.getSender().sendMessage(Language.getInstance().substitute(
        "{successPrefix} Updated lobby spawn of {0}!", arena.getName()));
  }

}
