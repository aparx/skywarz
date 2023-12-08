package io.github.aparx.skywarz.game.inventory.content;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.inventory.InventoryDimensions;
import io.github.aparx.skywarz.game.inventory.InventoryItem;
import io.github.aparx.skywarz.game.inventory.InventoryPosition;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Deterministic;

import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 03:49
 * @since 1.0
 */
public interface InventoryContentView {

  @Deterministic
  InventoryDimensions getDimensions();

  Optional<InventoryItem> find(@NonNegative int index);

  default Optional<InventoryItem> find(InventoryPosition pos) {
    return find(pos.toIndex(getDimensions().getWidth()));
  }

  default @NonNull InventoryItem get(@NonNegative int index) {
    return find(index).orElseThrow();
  }

  default @NonNull InventoryItem get(InventoryPosition pos) {
    return get(pos.toIndex(getDimensions().getWidth()));
  }

  static InventoryContentView empty(@NonNull InventoryDimensions dimensions) {
    Preconditions.checkNotNull(dimensions, "Dimensions must not be null");
    return new InventoryContentView() {
      @Override
      public InventoryDimensions getDimensions() {
        return dimensions;
      }

      @Override
      public Optional<InventoryItem> find(@NonNegative int index) {
        return Optional.empty();
      }
    };
  }

}
