package io.github.aparx.skywarz.command.commands.arena.set;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaSpawnCommand;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.handler.configs.Language;
import io.github.aparx.skywarz.skywars.arena.Arena;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 06:27
 * @since 1.0
 */
public class ArenaSetSpectatorCommand extends AbstractArenaSpawnCommand {

  public ArenaSetSpectatorCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
            .name("spectator")
            .aliases("spec")
            .args("<Arena>")
            .description("Update the spectator spawn to the current location")
            .build(),
        parent);
  }

  @Override
  protected void setLocation(
      Location location, Arena arena, CommandContext context, CommandArgList args) {
    arena.setSpectator(location);
    context.getSender().sendMessage(Language.getLanguage().substitute(
        "{successPrefix} Updated spectator spawn of {0}!", arena.getName()));
  }

}
