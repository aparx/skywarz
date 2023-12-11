package io.github.aparx.skywarz.command;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.*;
import io.github.aparx.skywarz.command.commands.arena.ArenaCreateCommand;
import io.github.aparx.skywarz.command.commands.arena.ArenaDeleteCommand;
import io.github.aparx.skywarz.command.commands.arena.ArenaListCommand;
import io.github.aparx.skywarz.command.commands.arena.ArenaSaveCommand;
import io.github.aparx.skywarz.command.commands.arena.add.ArenaAddSpawnCommand;
import io.github.aparx.skywarz.command.commands.arena.remove.ArenaRemoveSpawnCommand;
import io.github.aparx.skywarz.command.commands.arena.update.ArenaSetLobbyCommand;
import io.github.aparx.skywarz.command.commands.arena.update.ArenaSetPointCommand;
import io.github.aparx.skywarz.command.commands.arena.update.ArenaSetSpectatorCommand;
import io.github.aparx.skywarz.command.commands.arena.update.ArenaSetTeamSize;
import io.github.aparx.skywarz.command.commands.stats.StatsResetCommand;
import io.github.aparx.skywarz.command.tree.CommandBuilder;
import io.github.aparx.skywarz.command.tree.CommandNode;
import io.github.aparx.skywarz.command.tree.CommandNodeSet;
import io.github.aparx.skywarz.command.tree.CommandTree;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 05:27
 * @since 1.0
 */
public class SkywarsCommand implements CommandExecutor, TabCompleter {

  public static final CommandTree tree = buildTree();

  private static CommandTree buildTree() {
    CommandTree tree = new CommandTree();
    CommandNodeSet roots = tree.getRoots();
    // /skywars help <...>
    roots.add(new HelpCommand());

    // /skywars arena <...>
    final CommandNode arena;
    roots.add(arena = CommandBuilder.builder("arena")
        .args("<{children}>")
        .permission(SkywarsPermission.SETUP)
        .build());
    List.of(
        new ArenaCreateCommand(arena),
        new ArenaDeleteCommand(arena),
        new ArenaListCommand(arena),
        new ArenaSaveCommand(arena)
    ).forEach(arena::add);

    arena.add(CommandBuilder.builder(arena, "set")
        .permission(SkywarsPermission.SETUP)
        .args("<{children}>")
        .build()
        .add(ArenaSetSpectatorCommand::new)
        .add(ArenaSetLobbyCommand::new)
        .add(ArenaSetPointCommand::new)
        .add(ArenaSetTeamSize::new));

    arena.add(CommandBuilder.builder(arena, "add")
        .permission(SkywarsPermission.SETUP)
        .args("<{children}>")
        .build()
        .add(ArenaAddSpawnCommand::new));

    arena.add(CommandBuilder.builder(arena, "remove")
        .args("<{children}>")
        .permission(SkywarsPermission.SETUP)
        .build()
        .add(ArenaRemoveSpawnCommand::new));

    roots.add(new JoinCommand());
    roots.add(new LeaveCommand());
    roots.add(new StartCommand());

    StatsCommand stats = new StatsCommand();
    stats.add(new StatsResetCommand(stats));
    roots.add(stats);

    return tree;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label,
                           String[] args) {
    CommandArgList newArgs = CommandArgList.of(args);
    if (newArgs.isEmpty()) {
      Plugin plugin = Preconditions.checkNotNull(Skywars.plugin());
      Language language = Language.getInstance();
      sender.sendMessage(String.format(
          "%s %s v%s by aparx (@bonedfps)",
          language.get(MessageKeys.PREFIX).substitute(),
          plugin.getName(),
          plugin.getDescription().getVersion()));
    }
    tree.execute(new CommandContext(command, sender, newArgs, label), newArgs);
    return true;
  }


  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    final CommandArgList newArgs = CommandArgList.of(args);
    CommandContext context = new CommandContext(command, sender, newArgs, label);
    Optional<CommandNode> optNode = tree.locateLeaf(context, newArgs);
    if (context.isStatus(CommandContext.Status.ERROR_PERMISSION))
      return null;
    List<String> suggestions =
        optNode.<Collection<CommandNode>>map(CommandNode::getChildren)
            .orElseGet(tree::getRoots).stream()
            .filter((node) -> newArgs.length() <= 1 + node.getIndex())
            .filter((node) -> node.hasPermission(sender))
            .map((node) -> node.getInfo().getName())
            .collect(Collectors.toList());
    optNode.ifPresent((node) -> {
      List<String> list = node.onTabComplete(context, newArgs.subargs(1 + node.getIndex()));
      if (list != null && !list.isEmpty())
        suggestions.addAll(list);
    });
    return suggestions;
  }

}
