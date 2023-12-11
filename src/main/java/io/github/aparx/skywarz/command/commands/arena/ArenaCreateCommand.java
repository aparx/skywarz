package io.github.aparx.skywarz.command.commands.arena;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.game.arena.ArenaManager;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.language.Language;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.regex.Matcher;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 06:27
 * @since 1.0
 */
public class ArenaCreateCommand extends CommandNode {

  private static final int MAX_NAME_LENGTH = 20;

  public ArenaCreateCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
        .name("create")
        .args("<Name>")
        .description("Creates a new arena with given name")
        .build(), Preconditions.checkNotNull(parent));
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    if (args.length() != 1) context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      String name = args.last().get();
      Preconditions.checkState(!name.isEmpty() && name.length() <= MAX_NAME_LENGTH,
          String.format("Name must be less than %s characters", MAX_NAME_LENGTH));
      Matcher matcher = ArenaManager.NAME_PATTERN.matcher(name);
      Preconditions.checkState(matcher.matches(), "Name must not contain special characters");
      Skywars.getInstance().getArenaManager().register(new SkywarsArena(name));
      context.getSender().sendMessage(Language.getInstance().substitute(
          "{successPrefix} Created arena {0}!", name));
    }
  }

}
