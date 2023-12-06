package io.github.aparx.skywarz.game.inventory.content;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.game.inventory.InventoryDimensions;
import io.github.aparx.skywarz.game.inventory.InventoryItem;
import io.github.aparx.skywarz.game.inventory.InventoryPosition;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.SkullItem;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.util.NumberConversions;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 03:52
 * @since 1.0
 */
@SuppressWarnings("deprecation")
@Getter
public class PaginatableInventoryContent implements InventoryContentView {

  private final @NonNull Consumer<PaginatableInventoryContent> updater;

  private final @NonNull InventoryDimensions dimensions;

  private final ArrayList<@NonNull InventoryPage> pages = new ArrayList<>();

  private int pageIndex;

  private final InventoryPosition nextPagePos;
  private final InventoryPosition lastPagePos;

  private final InventoryItem nextPageItem = InventoryItem.of(
      SkullItem.of(ItemBuilder.builder().name(ChatColor.GRAY + "Next page").build(),
          Bukkit.getOfflinePlayer("MHF_ArrowRight")),
      (player, event) -> {
        paginateNext();
        event.setCancelled(true);
      });

  private final InventoryItem lastPageItem = InventoryItem.of(
      SkullItem.of(ItemBuilder.builder().name(ChatColor.GRAY + "Previous page").build(),
          Bukkit.getOfflinePlayer("MHF_ArrowLeft")),
      (player, event) -> {
        paginatePrevious();
        event.setCancelled(true);
      });

  private final InventoryItem noPageItem = InventoryItem.of(
      ItemBuilder.builder()
          .material(Material.GRAY_STAINED_GLASS_PANE)
          .name(StringUtils.SPACE)
          .build(),
      (p, e) -> e.setCancelled(true));

  public PaginatableInventoryContent(
      @NonNull Consumer<PaginatableInventoryContent> updater,
      @NonNull InventoryDimensions dimensions) {
    Preconditions.checkNotNull(dimensions, "Dimensions must not be null");
    Preconditions.checkNotNull(updater, "Updater must not be null");
    this.dimensions = dimensions;
    this.updater = updater;
    this.lastPagePos = getPaginationPosition(0);
    this.nextPagePos = getPaginationPosition(dimensions.getWidth() - 1);
  }

  public int getPageCount() {
    return getPages().size();
  }

  public void setPageIndex(int pageIndex) {
    Preconditions.checkElementIndex(pageIndex, pages.size());
    this.pageIndex = pageIndex;
  }

  public InventoryPage getPage() {
    if (pages.isEmpty()) return null;
    return pages.get(Math.min(getPageIndex(), pages.size() - 1));
  }

  public boolean hasNextPage() {
    return getPageIndex() < pages.size() - 1;
  }

  public boolean hasPreviousPage() {
    return getPageIndex() != 0 && !pages.isEmpty();
  }

  @CanIgnoreReturnValue
  public boolean paginateNext() {
    if (!hasNextPage()) return false;
    setPageIndex(1 + getPageIndex());
    updater.accept(this);
    return true;
  }

  @CanIgnoreReturnValue
  public boolean paginatePrevious() {
    if (!hasPreviousPage()) return false;
    setPageIndex(getPageIndex() - 1);
    updater.accept(this);
    return true;
  }

  @Override
  public Optional<InventoryItem> find(@NonNegative int index) {
    InventoryPage page = getPage();
    if (page == null) return Optional.empty();
    final int columnLength = dimensions.getWidth();
    // next page pagination item
    if (index == getNextPagePos().toIndex(columnLength))
      if (!hasNextPage()) return Optional.of(noPageItem);
      else return Optional.of(nextPageItem);
    // previous page pagination item
    if (index == getLastPagePos().toIndex(columnLength))
      if (!hasPreviousPage()) return Optional.of(noPageItem);
      else return Optional.of(lastPageItem);
    return page.find(index);
  }

  private InventoryPosition getPaginationPosition(int column) {
    return InventoryPosition.ofPoint(column,
        Math.max(NumberConversions.floor(getDimensions().getHeight() / 2.0) - 1, 0));
  }

}
