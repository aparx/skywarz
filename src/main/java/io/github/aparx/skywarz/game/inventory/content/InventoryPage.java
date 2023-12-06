package io.github.aparx.skywarz.game.inventory.content;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.game.inventory.InventoryDimensions;
import io.github.aparx.skywarz.game.inventory.InventoryItem;
import io.github.aparx.skywarz.game.inventory.InventoryPosition;
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
public class InventoryPage implements InventoryContentView {

  private final @NonNull InventoryDimensions dimensions;

  @Getter(AccessLevel.NONE)
  private final @Nullable InventoryItem @NonNull [] items;

  public InventoryPage(
      @NonNull InventoryDimensions dimensions,
      @Nullable InventoryItem @NonNull [] items) {
    Preconditions.checkNotNull(dimensions, "Dimensions must not be null");
    Preconditions.checkState(items.length == dimensions.size(),
        "Item size must equal the size of the dimensions given");
    Preconditions.checkNotNull(items, "Items must not be null");
    this.items = items;
    this.dimensions = dimensions;
  }

  public InventoryPage(@NonNull InventoryDimensions dimensions) {
    this(dimensions, new InventoryItem[dimensions.size()]);
  }

  @Override
  public Optional<InventoryItem> find(@NonNegative int index) {
    Preconditions.checkElementIndex(index, items.length);
    return Optional.ofNullable(items[index]);
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

  public void fillEdges(InventoryItem item) {
    InventoryDimensions dimensions = getDimensions();
    int columnLength = dimensions.getWidth();
    int rowLength = dimensions.getHeight();
    if (rowLength >= 1) // Fill top
      Arrays.fill(items, 0, columnLength, item);
    if (rowLength >= 2) // Fill bottom
      Arrays.fill(items,
          InventoryPosition.toIndex(0, rowLength - 1, columnLength),
          items.length - 1, item);
    fillSides(item);
  }

  public void fillSides(InventoryItem item) {
    int columnLength = dimensions.getWidth();
    int rowLength = dimensions.getHeight();
    // Fill left and right
    for (int rowIndex = 0; rowIndex < rowLength; ++rowIndex) {
      items[InventoryPosition.toIndex(0, rowIndex, columnLength)] = item;
      items[InventoryPosition.toIndex(columnLength - 1, rowIndex, columnLength)] = item;
    }
  }

}
