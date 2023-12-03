package io.github.aparx.skywarz.command.commands;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.SkywarsCommand;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.command.tree.CommandNodeSet;
import io.github.aparx.skywarz.handler.configs.Language;
import io.github.aparx.skywarz.utils.PaginationUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 04:38
 * @since 1.0
 */
public final class HelpCommand extends CommandNode {

  private static final int MAX_PER_PAGE = 4;

  @SuppressWarnings("Guava")
  private final Supplier<ImmutableList<CommandNode>> commands =
      Suppliers.memoize(new Supplier<ImmutableList<CommandNode>>() {
        @Override
        public ImmutableList<CommandNode> get() {
          CommandNodeSet roots = SkywarsCommand.tree.getRoots();
          ImmutableList.Builder<CommandNode> builder = ImmutableList.builder();
          for (CommandNode root : roots) accumulate(builder, root);
          return builder.build();
        }

        void accumulate(ImmutableList.Builder<CommandNode> accumulated, CommandNode node) {
          if (StringUtils.isNotEmpty(node.getInfo().getDescription()))
            accumulated.add(node);
          for (CommandNode child : node.getChildren())
            accumulate(accumulated, child);
        }
      });

  private final PaginationUtils.CommandChatPaginator<CommandNode> chatPaginator =
      PaginationUtils.CommandChatPaginator.<CommandNode>builder()
          .node(this)
          .title("Help")
          .maxPerPage(MAX_PER_PAGE)
          .nextPageCommandFactory((ctx, nextPageIndex) -> {
            // refers to "/sw help <PageIndex>" using the provided aliases used
            return String.format("/%s %s %s", ctx.getLabel(), getInfo().getName(), nextPageIndex);
          })
          .contentFactory((ctx, stream) -> {
            StringBuilder builder = new StringBuilder();
            Language language = Language.getLanguage();
            stream.forEach(node -> {
              builder.append(language.getPrefix()).append(ctx.isPlayer() ? " • " : " - ");
              builder.append(node.getUsage("sw")).append('\n');
              builder.append(language.getPrefix()).append(' ');
              builder.append(ChatColor.GRAY).append(node.getInfo().getDescription()).append('\n');
            });
            return builder.toString();
          })
          .build();

  public HelpCommand() {
    super(CommandInfo.builder()
        .name("help")
        .args("(Page)")
        .description("Show all descriptive commands")
        .build());
  }

  @Override
  public void execute(CommandContext context, CommandArgList argList) {
    chatPaginator.sendPage(commands.get().stream()
        .filter(node -> node.hasPermission(context.getSender()))
        .collect(Collectors.toList()), context, argList.isEmpty() ? 0 : argList.get(0).getInt(0));
  }


}
