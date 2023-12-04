package io.github.aparx.skywarz.game.inventory;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryType;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.Iterator;
import java.util.stream.IntStream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 13:36
 * @since 1.0
 */
@Getter
public final class InventoryDimensions {

  public static final InventoryDimensions CHEST =
      ofSize(InventoryType.CHEST.getDefaultSize(), 9);

  public static final InventoryDimensions WORKBENCH =
      ofSize(InventoryType.WORKBENCH.getDefaultSize(), 9);

  public static final InventoryDimensions ANVIL =
      ofSize(InventoryType.ANVIL.getDefaultSize(), 1);

  public static final InventoryDimensions BEACON =
      ofUnary(InventoryType.BEACON.getDefaultSize());

  private final @NonNegative int width;
  private final @NonNegative int height;

  private InventoryDimensions(int width, int height) {
    Preconditions.checkState(width >= 0, "width must be positive");
    Preconditions.checkState(height >= 0, "height must be positive");
    this.width = width;
    this.height = height;
  }

  public static InventoryDimensions ofUnary(@NonNegative int length) {
    return ofLengths(length, length);
  }

  public static InventoryDimensions ofSize(@NonNegative int size, @NonNegative int xLength) {
    return ofLengths(xLength, size / xLength);
  }

  public static InventoryDimensions ofLengths(@NonNegative int width, @NonNegative int height) {
    return new InventoryDimensions(width, height);
  }

  public int size() {
    return width * height;
  }

  public IntStream stream() {
    return IntStream.range(0, size());
  }

}