package io.github.aparx.skywarz.command.tree;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.exceptions.CommandError;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 02:42
 * @since 1.0
 */
@Getter
public final class CommandTree implements CommandNodeExecutor {

  private final CommandNodeSet roots = new CommandNodeSet(null);

  public static Map<String, Object> createCommandSubstitutorContext(CommandContext context) {
    HashMap<String, Object> valueMap = new HashMap<>();
    valueMap.put("sender", context.getSender().getName());
    valueMap.put("label", context.getLabel());
    valueMap.put("command", context.getCommand().getName());
    return valueMap;
  }

  private static CommandError createSyntaxError(CommandContext context, CommandNode node) {
    Map<String, Object> vals = createCommandSubstitutorContext(context);
    Optional.ofNullable(node)
        .map(CommandNode::getUsage)
        .ifPresent((usage) -> vals.put("usage", usage));
    return new CommandError((lang) -> lang.substitute(MessageKeys.Errors.SYNTAX, vals));
  }

  private static CommandError createPermissionError(CommandContext context) {
    Map<String, Object> vals = createCommandSubstitutorContext(context);
    return new CommandError((lang) -> lang.substitute(MessageKeys.Errors.PERMISSION, vals));
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    CommandSender sender = context.getSender();
    try {
      CommandNode node = locateLeaf(context, args).orElseThrow(() -> {
        Map<String, Object> vals = createCommandSubstitutorContext(context);
        return new CommandError((l) -> l.substitute(MessageKeys.Errors.COMMAND_NOT_FOUND, vals));
      });
      handleErrors(context, args, node);
      node.execute(context, args.subargs(1 + node.getIndex()));
      handleErrors(context, args, node);
    } catch (CommandError error) {
      sender.sendMessage(error.getMessage());
    } catch (IllegalArgumentException | IllegalStateException e) {
      e.printStackTrace(); // TODO only for development mode
      Skywars.logger().log(Level.FINE, "Error occurred on command execution", e);
      sender.sendMessage(Language.getInstance().substitute(MessageKeys.Errors.GENERIC, e.getMessage()));
    }
  }

  public Optional<CommandNode> locateLeaf(CommandContext context, CommandArgList args) {
    if (args.isEmpty()) return Optional.empty();
    String command = args.first().get();
    for (CommandNode root : roots) {
      if (!root.isMatching(command)) continue;
      return Optional.ofNullable(locateLeaf0(context, args.subargs(1), root));
    }
    return Optional.empty();
  }

  private CommandNode locateLeaf0(CommandContext context, CommandArgList args, CommandNode node) {
    for (String argument : args.toStringArray()) {
      Optional<CommandNode> next = node.getChildren().stream()
          .filter(child -> child.isMatching(argument))
          .findFirst();
      if (next.isEmpty()) break;
      node = next.get();
    }
    if (!node.hasPermission(context.getSender()))
      context.setStatus(CommandContext.Status.ERROR_PERMISSION);
    return node;
  }

  private void handleErrors(CommandContext context, CommandArgList args, CommandNode node) {
    if (context.isStatus(CommandContext.Status.ERROR_PERMISSION))
      throw createPermissionError(context);
    if (context.isStatus(CommandContext.Status.ERROR_SYNTAX))
      throw createSyntaxError(context, node);
    if (context.isError())
      throw new IllegalArgumentException("Error occurred");
  }
}
