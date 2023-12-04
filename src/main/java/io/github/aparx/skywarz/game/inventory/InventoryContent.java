package io.github.aparx.skywarz.game.inventory;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 13:11
 * @since 1.0
 */
@Getter
public class InventoryContent {

  private final @NonNull InventoryDimensions dimensions;

  @Getter(AccessLevel.NONE)
  private final @Nullable InventoryItem @NonNull [] items;

  public InventoryContent(
      @NonNull InventoryDimensions dimensions,
      @Nullable InventoryItem @NonNull [] items) {
    Preconditions.checkNotNull(dimensions, "Dimensions must not be null");
    Preconditions.checkState(items.length == dimensions.size(),
        "Item size must equal the size of the dimensions given");
    Preconditions.checkNotNull(items, "Items must not be null");
    this.items = items;
    this.dimensions = dimensions;
  }

  public InventoryContent(@NonNull InventoryDimensions dimensions) {
    this(dimensions, new InventoryItem[dimensions.size()]);
  }

  public Optional<InventoryItem> find(@NonNegative int index) {
    Preconditions.checkElementIndex(index, items.length);
    return Optional.ofNullable(items[index]);
  }

  public Optional<InventoryItem> find(InventoryPosition pos) {
    return find(pos.toIndex(getDimensions().getWidth()));
  }

  public @NonNull InventoryItem get(@NonNegative int index) {
    return find(index).orElseThrow();
  }

  public @NonNull InventoryItem get(InventoryPosition pos) {
    return get(pos.toIndex(getDimensions().getWidth()));
  }

  @CanIgnoreReturnValue
  public @Nullable InventoryItem set(@NonNegative int index, InventoryItem item) {
    Preconditions.checkElementIndex(index, items.length);
    InventoryItem previous = items[index];
    items[index] = item;
    return previous;
  }

  @CanIgnoreReturnValue
  public @Nullable InventoryItem set(@NonNegative int index, ItemStack stack) {
    return set(index, InventoryItem.of(stack));
  }

  @CanIgnoreReturnValue
  public @Nullable InventoryItem set(InventoryPosition pos, InventoryItem item) {
    return set(pos.toIndex(getDimensions().getWidth()), item);
  }

  @CanIgnoreReturnValue
  public @Nullable InventoryItem set(InventoryPosition pos, ItemStack stack) {
    return set(pos, InventoryItem.of(stack));
  }

  public void fill(InventoryItem item) {
    Arrays.fill(items, item);
  }

}
