package io.github.aparx.skywarz.command.commands.arena;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.skywars.arena.Arena;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 08:10
 * @since 1.0
 */
public abstract class AbstractArenaSpawnCommand extends AbstractArenaCommand {

  public AbstractArenaSpawnCommand(
      @NonNull CommandInfo info,
      @NonNull CommandNode parent,
      @NonNegative int arenaArgumentIndex) {
    super(info, parent, arenaArgumentIndex);
  }

  public AbstractArenaSpawnCommand(@NonNull CommandInfo info, @NonNull CommandNode parent) {
    super(info, parent, 0);
  }

  protected abstract void setLocation(
      Location location, Arena arena, CommandContext context, CommandArgList args);

  @Override
  public void execute(Arena arena, CommandContext context, CommandArgList args) {
    Player player = context.getPlayer();
    Location location = player.getLocation().clone();
    World world = location.getWorld();
    for (int i = 0; i < 2; ++i) {
      location.setY(1 + location.getBlockY());
      if (world.getBlockAt(location).getType().isSolid())
        throw new IllegalStateException("Not enough space for the spawn! (above?)");
    }
    setLocation(player.getLocation(), arena, context, args);
  }

}
