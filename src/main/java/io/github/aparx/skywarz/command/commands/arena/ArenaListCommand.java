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
import io.github.aparx.skywarz.utils.PaginationUtils;
import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Comparator;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 06:27
 * @since 1.0
 */
public class ArenaListCommand extends CommandNode {

  private static final int MAX_PER_PAGE = 7;

  private static final Comparator<GameArena> COMPLETION_COMPARATOR = (a, b) ->
      a.isCompleted() == b.isCompleted() ? 0 : a.isCompleted() ? 1 : -1;

  private final PaginationUtils.CommandChatPaginator<GameArena> chatPaginator =
      PaginationUtils.CommandChatPaginator.<GameArena>builder()
          .node(this)
          .title("Arenas")
          .maxPerPage(MAX_PER_PAGE)
          .nextPageCommandFactory((c, i) -> String.format("/%s arena list %s", c.getLabel(), i))
          .contentFactory((ctx, stream) -> {
            StringBuilder builder = new StringBuilder();
            Language language = Language.getInstance();
            stream.sorted(COMPLETION_COMPARATOR).forEach(arena -> {
              builder.append(language.get(MessageKeys.PREFIX).get());
              builder.append(ctx.isPlayer() ? " • " : " - ");
              builder.append(arena.getName()).append(' ');
              if (arena.isCompleted())
                builder.append(ChatColor.GRAY).append(ChatColor.ITALIC).append("(completed)");
              else
                builder.append(ChatColor.RED).append(ChatColor.ITALIC).append("(uncompleted)");
              builder.append('\n');
            });
            if (builder.length() == 0)
              builder.append(ChatColor.GRAY).append("No arenas available").append('\n');
            return builder.toString();
          })
          .build();

  public ArenaListCommand(@NonNull CommandNode parent) {
    super(CommandInfo.builder()
        .name("list")
        .aliases("show")
        .args("(Page)")
        .description("Shows a list of all arenas existing")
        .build(), Preconditions.checkNotNull(parent));
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    Collection<GameArena> arenas = Skywars.getInstance().getArenaManager().asSet();
    chatPaginator.sendPage(arenas, context, args.isEmpty() ? 0 : args.get(0).getInt(0));
  }

}
