package io.github.aparx.skywarz.command.commands.arena;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 06:27
 * @since 1.0
 */
public class ArenaDeleteCommand extends AbstractArenaCommand {

  private static final int CONFIRM_ARGUMENT_INDEX = 1;

  private static final int ARENA_ARGUMENT_INDEX = 0;

  public ArenaDeleteCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
            .name("delete")
            .args("<Arena>")
            .description("Deletes an arena with given name")
            .build(),
        ARENA_ARGUMENT_INDEX,
        Preconditions.checkNotNull(parent));
  }

  @Override
  protected void execute(GameArena arena, CommandContext context, CommandArgList args) {
    Language language = Language.getInstance();
    CommandSender sender = context.getSender();
    if (args.length() > CONFIRM_ARGUMENT_INDEX) {
      deleteArena(arena, context);
    } else if (context.isPlayer()) {
      String prefix = language.get(MessageKeys.PREFIX).get();
      // Confirmation of deletion required (since it is called in game)
      sender.sendMessage(String.format("%s Are you sure you want to delete %s?",
          language.get(MessageKeys.PREFIX).get(), arena.getName()));
      TextComponent component = new TextComponent(prefix + ' ');
      TextComponent confirmButton = new TextComponent("§a§l[CONFIRM DELETION]");
      confirmButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(
          "/%s arena delete %s %s", context.getLabel(), arena.getName(), "confirm")));
      component.addExtra(confirmButton);
      context.getPlayer().spigot().sendMessage(component);
    } else {
      deleteArena(arena, context);
    }
  }

  private void deleteArena(@NonNull GameArena arena, @NonNull CommandContext context) {
    // confirmation and consent has been given, continue deletion
    Preconditions.checkState(Skywars.getInstance().getArenaManager().delete(arena),
        String.format("Could not delete arena '%s' (not found?)", arena.getName()));
    context.getSender().sendMessage(Language.getInstance().substitute(
        "{successPrefix} Arena '{0}' was deleted successfully!",
        arena.getName()));
    // remove arena signs in the physical block world
    arena.getSignHandler().getRegister().getCollection().forEach((sign) -> {
      Block block = sign.getLocation().getBlock();
      if (block.getState() instanceof Sign)
        block.setType(Material.AIR);
    });
  }
}
