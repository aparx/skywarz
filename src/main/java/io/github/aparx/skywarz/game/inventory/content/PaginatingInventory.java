package io.github.aparx.skywarz.game.inventory.content;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.inventory.SpecialInventory;
import io.github.aparx.skywarz.game.inventory.InventoryDimensions;
import io.github.aparx.skywarz.game.inventory.InventoryItem;
import io.github.aparx.skywarz.game.inventory.InventoryPosition;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.InventoryHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 04:51
 * @since 1.0
 */
@Getter
@Setter
public class PaginatingInventory extends SpecialInventory<PaginatableInventoryContent> {

  private final List<InventoryItem> elements;

  private final InventoryDimensions maxDimensions;

  public PaginatingInventory(
      @Nullable InventoryHolder holder,
      @NonNull TickDuration updateInterval,
      @NonNull InventoryDimensions initialDimensions,
      @NonNull List<InventoryItem> elements,
      @NonNull String title) {
    this(holder, updateInterval, initialDimensions, initialDimensions, elements, title);
  }

  public PaginatingInventory(
      @Nullable InventoryHolder holder,
      @NonNull TickDuration updateInterval,
      @NonNull InventoryDimensions minDimensions,
      @NonNull InventoryDimensions maxDimensions,
      @NonNull List<InventoryItem> elements,
      @NonNull String title) {
    super(holder, updateInterval, title, null);
    Preconditions.checkNotNull(elements, "Elements must not be null");
    Preconditions.checkNotNull(minDimensions, "minDimensions must not be null");
    Preconditions.checkNotNull(maxDimensions, "maxDimensions must not be null");
    Preconditions.checkState(minDimensions.size() <= maxDimensions.size(),
        "minDimensions > maxDimensions (has to be less or equal)");
    this.elements = elements;
    this.maxDimensions = maxDimensions;
    updateDimensions(minDimensions);
  }

  public void updateDimensions(InventoryDimensions dimensions) {
    Preconditions.checkState(dimensions.size() <= maxDimensions.size(),
        "Inventory size is larger than the maximum has defined");
    setContent(new PaginatableInventoryContent((x) -> updateInventory(), dimensions));
  }

  @Override
  public void updateInventory(long ticks) {
    InventoryDimensions dimensions = getDimensions();
    PaginatableInventoryContent content = getContent();
    ArrayList<@NonNull InventoryPage> pages = content.getPages();
    final int count = elements.size();
    if (!getMaxDimensions().equals(getDimensions())) {
      int calculatedHeight = Math.min(1 + count / (dimensions.getWidth() - 2),
          getMaxDimensions().getHeight());
      if (dimensions.getHeight() < calculatedHeight) {
        updateDimensions(dimensions.withHeight(calculatedHeight));
        return;
      }
    }
    final int columnLength = dimensions.getWidth();
    final int rowLength = dimensions.getHeight();
    final int columnMod = columnLength - 2;
    if (pages.size() != Math.ceil((double) count / (rowLength * columnMod))) {
      pages.clear();
      InventoryPage page = new InventoryPage(dimensions);
      page.fillSides(getContent().getNoPageItem());
      int pageAccumulate = 0;
      for (int i = 0; i < count; ++i) {
        page.set(InventoryPosition.ofPoint(1 + (i % columnMod), pageAccumulate), elements.get(i));
        if ((1 + i) % columnMod == 0 && ++pageAccumulate >= rowLength) {
          pages.add(page);
          page.fillSides(getContent().getNoPageItem());
          page = new InventoryPage(dimensions);
          pageAccumulate = 0;
        }
      }
      if (count % columnMod != 0) {
        page.fillSides(getContent().getNoPageItem());
        pages.add(page);
      }
    }
    super.updateInventory(ticks);
  }

}
