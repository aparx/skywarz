package io.github.aparx.skywarz.command.tree;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.exceptions.CommandError;
import io.github.aparx.skywarz.handler.configs.Language;
import lombok.Getter;
import org.apache.commons.text.StringSubstitutor;
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
    Map<String, Object> valueMap = createCommandSubstitutorContext(context);
    Optional.ofNullable(node)
        .map(CommandNode::getUsage)
        .ifPresent((usage) -> valueMap.put("usage", usage));
    return new CommandError((t, lang) -> lang.substitute(lang.getErrorCommandSyntax(), valueMap));
  }

  private static CommandError createPermissionError(CommandContext context) {
    Map<String, Object> valueMap = createCommandSubstitutorContext(context);
    return new CommandError((t, lang) -> lang.substitute(lang.getErrorPermission(), valueMap));
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    CommandSender sender = context.getSender();
    try {
      CommandNode node = locateLeaf(context, args).orElseThrow(() -> {
        Map<String, Object> valueMap = createCommandSubstitutorContext(context);
        return new CommandError((t, l) -> l.substitute(l.getErrorCommandNotFound(), valueMap));
      });
      handleErrors(context, args, node);
      node.execute(context, args.subargs(1 + node.getIndex()));
      handleErrors(context, args, node);
    } catch (CommandError error) {
      sender.sendMessage(error.getMessageFromLanguage());
    } catch (IllegalArgumentException | IllegalStateException exception) {
      exception.printStackTrace(); // TODO only for development mode
      Skywars.logger().log(Level.FINE, "Error occurred on command execution", exception);
      sender.sendMessage(Language.getLanguage().substitute((lang) -> lang.substitute(lang.getError(),
          Map.of("message", exception.getMessage()))));
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
