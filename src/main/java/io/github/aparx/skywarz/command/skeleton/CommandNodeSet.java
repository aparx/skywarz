package io.github.aparx.skywarz.command.skeleton;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.utils.collection.AbstractKeyValueSet;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 02:35
 * @since 1.0
 */
@Getter
public class CommandNodeSet extends AbstractKeyValueSet<String, CommandNode> {

  private final @Nullable CommandNode parent;

  public CommandNodeSet(@Nullable CommandNode parent) {
    super(new LinkedHashMap<>());
    this.parent = parent;
  }

  @Override
  public String getKey(CommandNode commandNode) {
    return commandNode.getInfo().getName();
  }

  @Override
  public boolean add(CommandNode node) {
    Preconditions.checkNotNull(node, "Node must not be null");
    if (!super.add(node)) return false;
    Preconditions.checkState(Objects.equals(parent, node.getParent()),
        "Child must have same parent to which it is added to");
    return super.add(node);
  }
}
