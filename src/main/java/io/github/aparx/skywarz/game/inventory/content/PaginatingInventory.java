package io.github.aparx.skywarz.game.inventory.content;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.inventory.GameInventory;
import io.github.aparx.skywarz.game.inventory.InventoryDimensions;
import io.github.aparx.skywarz.game.inventory.InventoryItem;
import io.github.aparx.skywarz.game.inventory.InventoryPosition;
import io.github.aparx.skywarz.utils.tick.TickDuration;
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
public class PaginatingInventory extends GameInventory<PaginatableInventoryContent> {

  private final List<InventoryItem> elements;

  public PaginatingInventory(
      @Nullable InventoryHolder holder,
      @NonNull TickDuration updateInterval,
      @NonNull InventoryDimensions dimensions,
      @NonNull List<InventoryItem> elements,
      @NonNull String title) {
    super(holder, updateInterval, title, null);
    Preconditions.checkNotNull(elements, "Elements must not be null");
    this.elements = elements;
    setContent(new PaginatableInventoryContent((x) -> updateInventory(), dimensions));
  }

  @Override
  public void updateInventory(long ticks) {
    InventoryDimensions dimensions = getDimensions();
    int columnLength = dimensions.getWidth();
    int rowLength = dimensions.getHeight();
    PaginatableInventoryContent content = getContent();
    ArrayList<@NonNull InventoryPage> pages = content.getPages();
    int count = elements.size();
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
