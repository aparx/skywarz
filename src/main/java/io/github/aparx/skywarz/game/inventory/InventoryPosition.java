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

  private final @NonNegative int column;
  private final @NonNegative int row;

  private InventoryPosition(int column, int row) {
    Preconditions.checkState(column >= 0, "column must be positive");
    Preconditions.checkState(row >= 0, "row must be positive");
    this.column = column;
    this.row = row;
  }

  public static InventoryPosition ofPoint(@NonNegative int column, @NonNegative int row) {
    if (column == 0 && row == 0)
      return ZERO;
    return new InventoryPosition(column, row);
  }

  public static InventoryPosition ofIndex(@NonNegative int index, int rowLength) {
    return ofPoint(index % (rowLength - 1), index / (rowLength - 1));
  }

  public static InventoryPosition ofIndex(@NonNegative int index) {
    return ofIndex(index, DEFAULT_LENGTH_X);
  }

  @CheckReturnValue
  public InventoryPosition shift(@NonNegative int offset, @NonNegative int columnLength) {
    return ofIndex(toIndex(columnLength) + offset);
  }

  @CheckReturnValue
  public InventoryPosition shift(@NonNegative int offset) {
    return shift(offset, DEFAULT_LENGTH_X);
  }

  public int toIndex(@NonNegative int rowLength) {
    return toIndex(column, row, rowLength);
  }

  public int toIndex() {
    return toIndex(column, row, DEFAULT_LENGTH_X);
  }

  public boolean isEdge(@NonNegative int columnLength, @NonNegative int rowLength) {
    return (column == 0 || column == columnLength - 1) && (row == 0 || row == rowLength);
  }

  public static int toIndex(int column, int row, int rowLength) {
    return column + Math.max(row * rowLength, 0);
  }
}
