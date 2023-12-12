package io.github.aparx.skywarz.command.commands.arena.update;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaSpawnCommand;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.game.arena.ArenaData;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
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
      Location location, GameArena arena, CommandContext context, CommandArgList args) {
    ArenaData data = arena.getData();
    if (data.getBox().isCompleted() && data.getBox().isWithin(location))
      throw new IllegalArgumentException("Lobby spawn must be outside the arena!");
    data.setLobby(location);
    context.getSender().sendMessage(Language.getInstance().substitute(
        "{successPrefix} Updated lobby spawn of '{0}'. (unsaved)",
        arena.getName()));
  }

}
