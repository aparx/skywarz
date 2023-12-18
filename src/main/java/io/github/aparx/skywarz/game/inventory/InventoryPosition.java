package io.github.aparx.skywarz.game.inventory;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CheckReturnValue;
import lombok.Getter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Iterator;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 13:16
 * @since 1.0
 */
@Getter
public final class InventoryPosition {

  private static final InventoryPosition ZERO = new InventoryPosition(0, 0);

  private final @NonNegative int column;
  private final @NonNegative int row;

  private InventoryPosition(int column, int row) {
    Preconditions.checkState(column >= 0, "column must be positive");
    Preconditions.checkState(row >= 0, "row must be positive");
    this.column = column;
    this.row = row;
  }

  public static InventoryPositionInterpolator interpolate(
      @NonNull InventoryPosition begin, @NonNull InventoryPosition end) {
    return new InventoryPositionInterpolator(begin, end);
  }

  public static InventoryPosition getMin(InventoryPosition a, InventoryPosition b) {
    return a.toIndex() <= b.toIndex() ? a : b;
  }

  public static InventoryPosition getMax(InventoryPosition a, InventoryPosition b) {
    return a.toIndex() >= b.toIndex() ? a : b;
  }

  public static int toIndex(int column, int row, int columnLength) {
    return column + Math.max(row * columnLength, 0);
  }

  public static InventoryPosition ofPoint(@NonNegative int column, @NonNegative int row) {
    if (column == 0 && row == 0)
      return ZERO;
    return new InventoryPosition(column, row);
  }

  public static InventoryPosition ofIndex(@NonNegative int index, int columnLength) {
    return ofPoint(index % columnLength, index / columnLength);
  }

  public static InventoryPosition ofIndex(@NonNegative int index) {
    return ofIndex(index, InventoryDimensions.DEFAULT_COLUMN_COUNT);
  }

  @CheckReturnValue
  public InventoryPosition shift(int offset, int columnLength) {
    return ofIndex(toIndex(columnLength) + offset);
  }

  @CheckReturnValue
  public InventoryPosition shift(int offset) {
    return shift(offset, InventoryDimensions.DEFAULT_COLUMN_COUNT);
  }

  @CheckReturnValue
  public InventoryPosition add(int columnOffset, int rowOffset) {
    return new InventoryPosition(this.column + columnOffset, this.row + rowOffset);
  }

  public int toIndex(@NonNegative int rowLength) {
    return toIndex(column, row, rowLength);
  }

  public int toIndex() {
    return toIndex(column, row, InventoryDimensions.DEFAULT_COLUMN_COUNT);
  }

  public boolean isEdge(@NonNegative int columnLength, @NonNegative int rowLength) {
    return (column == 0 || column == columnLength - 1) && (row == 0 || row == rowLength);
  }


  @Override
  public String toString() {
    return "InventoryPosition{" +
        "column=" + column +
        ", row=" + row +
        '}';
  }
}
