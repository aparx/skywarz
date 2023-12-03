package io.github.aparx.skywarz.command.commands.arena;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.handler.configs.Language;
import io.github.aparx.skywarz.skywars.arena.Arena;
import io.github.aparx.skywarz.skywars.arena.ArenaManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 06:27
 * @since 1.0
 */
public class ArenaDeleteCommand extends AbstractArenaCommand {

  private static final String CONFIRM_ARGUMENT = "confirm";

  private static final int CONFIRM_ARGUMENT_INDEX = 1;

  private static final int ARENA_ARGUMENT_INDEX = 0;

  public ArenaDeleteCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
            .name("delete")
            .args("<Arena>")
            .description("Deletes an arena with given name")
            .build(), Preconditions.checkNotNull(parent),
        ARENA_ARGUMENT_INDEX);
  }

  @Override
  protected void execute(Arena arena, CommandContext context, CommandArgList args) {
    Language language = Language.getLanguage();
    CommandSender sender = context.getSender();
    ArenaManager arenaManager = Skywars.getInstance().getArenaManager();
    if (args.length() > CONFIRM_ARGUMENT_INDEX
        && CONFIRM_ARGUMENT.equalsIgnoreCase(args.getString(CONFIRM_ARGUMENT_INDEX))
        || !context.isPlayer()) {
      // confirmation and consent has been given, continue deletion
      Preconditions.checkState(arenaManager.delete(arena),
          String.format("Could not delete arena %s (not found?)", arena.getName()));
      sender.sendMessage(Language.getLanguage().substitute(
          "{successPrefix} Deleted arena {0}!", arena.getName()));
    } else {
      // Confirmation of deletion required (since it is called in game)
      sender.sendMessage(String.format("%s Are you sure you want to delete %s?",
          language.getPrefix(), arena.getName()));
      TextComponent component = new TextComponent(language.getPrefix() + ' ');
      TextComponent confirmButton = new TextComponent("§a§l[CONFIRM DELETION]");
      confirmButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(
          "/%s arena delete %s %s", context.getLabel(), arena.getName(), CONFIRM_ARGUMENT)));
      component.addExtra(confirmButton);
      context.getPlayer().spigot().sendMessage(component);
    }
  }
}
