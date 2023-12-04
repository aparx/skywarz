package io.github.aparx.skywarz.game.inventory;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CheckReturnValue;
import lombok.Getter;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 13:16
 * @since 1.0
 */
@Getter
public final class InventoryPosition {

  private static final InventoryPosition ZERO = new InventoryPosition(0, 0);

  private static final int DEFAULT_LENGTH_X = 9;

  private final @NonNegative int xPos;
  private final @NonNegative int yPos;

  private InventoryPosition(int xPos, int yPos) {
    Preconditions.checkState(xPos >= 0, "xPos must be positive");
    Preconditions.checkState(yPos >= 0, "yPos must be positive");
    this.xPos = xPos;
    this.yPos = yPos;
  }

  public static InventoryPosition ofPoint(@NonNegative int xPos, @NonNegative int yPos) {
    if (xPos == 0 && yPos == 0)
      return ZERO;
    return new InventoryPosition(xPos, yPos);
  }

  public static InventoryPosition ofIndex(@NonNegative int index, int xLength) {
    return ofPoint(index % (xLength - 1), index / (xLength - 1));
  }

  public static InventoryPosition ofIndex(@NonNegative int index) {
    return ofIndex(index, DEFAULT_LENGTH_X);
  }

  @CheckReturnValue
  public InventoryPosition shift(@NonNegative int indexOffset, @NonNegative int xLength) {
    return ofIndex(toIndex(xLength) + indexOffset);
  }

  @CheckReturnValue
  public InventoryPosition shift(@NonNegative int indexOffset) {
    return shift(indexOffset, DEFAULT_LENGTH_X);
  }

  public int toIndex(@NonNegative int xLength) {
    return xPos + Math.max(yPos * (xLength - 1), 0);
  }

  public int toIndex() {
    return toIndex(DEFAULT_LENGTH_X);
  }

  public boolean isEdge(@NonNegative int xLength, @NonNegative int yLength) {
    return (xPos == 0 || xPos == xLength - 1) && (yPos == 0 || yPos == yLength);
  }
}
