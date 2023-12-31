package io.github.aparx.skywarz.command.skeleton;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.permissions.Permissible;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 02:28
 * @since 1.0
 */
@SuppressWarnings("Guava")
@Getter
public abstract class CommandNode implements CommandNodeExecutor {

  private final @NonNull CommandInfo info;
  private final @NonNull CommandNode root;
  private final CommandNode parent;
  private final int index;

  private final @NonNull CommandNodeSet children = new CommandNodeSet(this);

  @Getter(AccessLevel.NONE)
  private final Supplier<String> argsProcessed = Suppliers.memoize(() -> {
    StringBuilder builder = new StringBuilder();
    for (CommandNode p = this; p != null; p = p.getParent()) {
      if (builder.length() != 0) builder.insert(0, ' ');
      builder.insert(0, p.getInfo().getName());
    }
    return builder.toString();
  });

  @Getter(AccessLevel.NONE)
  private final Supplier<String> usageProcessed = Suppliers.memoize(() -> {
    // substitutes string variables within the usage for further "usage"
    String usage = getInfo().getUsage();
    if (usage == null) return "null";
    return Language.getInstance().substitute(usage, createValueMapForNode(this));
  });

  public CommandNode(@NonNull CommandInfo info) {
    this(info, null);
  }

  public CommandNode(@NonNull CommandInfo info, @Nullable CommandNode parent) {
    Preconditions.checkNotNull(info, "Info must not be null");
    this.info = info;
    this.parent = parent;
    this.root = parent != null ? parent.root : this;
    int position = 0;
    //noinspection StatementWithEmptyBody
    for (CommandNode p = this; (p = p.getParent()) != null; ++position) ;
    this.index = position;
  }

  public static Map<String, Object> createValueMapForNode(@NonNull CommandNode node) {
    HashMap<String, Object> map = new HashMap<>();
    createValueMapForNode0(map, node, null);
    return map;
  }

  private static void createValueMapForNode0(
      @NonNull Map<String, Object> output, @NonNull CommandNode node, String prefix) {
    CommandInfo info = node.getInfo();
    String keyPrefix = StringUtils.isEmpty(prefix) ? StringUtils.EMPTY : prefix;
    output.put(keyPrefix + "args", node.getCommandArgs());
    output.put(keyPrefix + "name", info.getName());
    output.put(keyPrefix + "usage", info.getUsage());
    output.put(keyPrefix + "aliases", info.getAliases());
    output.put(keyPrefix + "children", node.getChildren().stream()
        .map((child) -> child.getInfo().getName())
        .collect(Collectors.joining(":")));
    if (!node.isRoot())
      createValueMapForNode0(output, node.getParent(), keyPrefix + "parent.");
  }

  @Override
  public List<String> onTabComplete(CommandContext context, CommandArgList args) {
    if (args.length() == 1 && !children.isEmpty()) {
      String arg0 = args.first().get().toLowerCase();
      return children.stream()
          .filter((node) -> node.hasPermission(context.getSender()))
          .map(CommandNode::getInfo)
          .map(CommandInfo::getName)
          .filter((str) -> StringUtils.startsWithIgnoreCase(str, arg0))
          .collect(Collectors.toList());
    }
    return null;
  }

  public boolean isMatching(String argument) {
    if (info.getName().equalsIgnoreCase(argument))
      return true;
    return info.getAliases().stream()
        .anyMatch((alias) -> alias.equalsIgnoreCase(argument));
  }

  public boolean hasPermission(Permissible permissible) {
    for (CommandNode p = this; p != null; p = p.getParent()) {
      SkywarsPermission permission = p.getInfo().getPermission();
      if (permission != null && !permission.has(permissible))
        return false;
    }
    return true;
  }

  /** Returns the proper usage without substitution of any command execution dependant variables. */
  public String getUsage() {
    return usageProcessed.get();
  }

  /** Returns the proper usage string with substitution of the label (root command). */
  public String getUsage(@NonNull String label) {
    return Language.getInstance().substitute(usageProcessed.get(), Map.of("label", label));
  }

  /** Returns the command arguments to reach this node through a command line. */
  public String getCommandArgs() {
    return argsProcessed.get();
  }

  public String createCommand(@NonNull String label) {
    return label + ' ' + getCommandArgs();
  }

  @CanIgnoreReturnValue
  public CommandNode add(@NonNull CommandNode child) {
    Preconditions.checkNotNull(child, "Child must not be null");
    getChildren().add(child);
    return this;
  }

  @CanIgnoreReturnValue
  public CommandNode add(@NonNull Function<CommandNode, @NonNull CommandNode> child) {
    return add(Preconditions.checkNotNull(child.apply(this)));
  }

  public boolean isRoot() {
    return getParent() == null && equals(getRoot());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CommandNode that = (CommandNode) o;
    return Objects.equals(info, that.info)
        && Objects.equals(parent, that.parent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(info, parent);
  }
}
