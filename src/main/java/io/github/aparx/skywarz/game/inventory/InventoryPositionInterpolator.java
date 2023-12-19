package io.github.aparx.skywarz.game.inventory;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-18 21:41
 * @since 1.0
 */
@Getter
public class InventoryPositionInterpolator implements Iterable<InventoryPosition> {

  private final @NonNull InventoryPosition begin, end;

  @Getter(AccessLevel.NONE)
  private final int startIndex, stopIndex;

  public InventoryPositionInterpolator(
      @NonNull InventoryPosition begin, @NonNull InventoryPosition end) {
    Preconditions.checkNotNull(begin, "Begin must not be null");
    Preconditions.checkNotNull(end, "End must not be null");
    this.begin = InventoryPosition.getMin(begin, end);
    this.end = InventoryPosition.getMax(begin, end);
    this.startIndex = this.begin.toIndex();
    this.stopIndex = this.end.toIndex();
  }

  public InventoryPositionInterpolator expand(int column, int row) {
    return new InventoryPositionInterpolator(begin.add(-column, -row), end.add(column, row));
  }

  @Override
  public @NonNull Iterator<InventoryPosition> iterator() {
    return new Iterator<>() {

      private int cursor;

      @Override
      public boolean hasNext() {
        return startIndex + cursor <= stopIndex;
      }

      @Override
      public InventoryPosition next() {
        return InventoryPosition.ofIndex(startIndex + (cursor++));
      }
    };
  }
}
