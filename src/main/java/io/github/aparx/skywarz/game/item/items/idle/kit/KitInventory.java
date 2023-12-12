package io.github.aparx.skywarz.game.item.items.idle.kit;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.entity.GamePlayer;
import io.github.aparx.skywarz.game.inventory.SpecialInventory;
import io.github.aparx.skywarz.game.inventory.InventoryDimensions;
import io.github.aparx.skywarz.game.inventory.InventoryItem;
import io.github.aparx.skywarz.game.inventory.InventoryPosition;
import io.github.aparx.skywarz.game.inventory.content.InventoryPage;
import io.github.aparx.skywarz.game.inventory.content.PaginatableInventoryContent;
import io.github.aparx.skywarz.game.inventory.content.PaginatingInventory;
import io.github.aparx.skywarz.game.kit.SkywarsKit;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.scoreboard.MatchScoreboard;
import io.github.aparx.skywarz.game.scoreboard.SpecialScoreboard;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-08 00:39
 * @since 1.0
 */
@Getter
public class KitInventory extends PaginatingInventory {

  private static final InventoryDimensions DIMENSION_MIN = InventoryDimensions.ofRows(1);
  private static final InventoryDimensions DIMENSION_MAX = InventoryDimensions.ofRows(4);

  private static final InventoryDimensions KIT_DIMENSION = InventoryDimensions.ofRows(4);

  private static final InventoryPosition PREVIOUS_PAGE = InventoryPosition.ofPoint(3, 3);
  private static final InventoryPosition NEXT_PAGE = InventoryPosition.ofPoint(4, 3);

  private static final InventoryPosition KIT_BUTTON_EQUIP = InventoryPosition.ofPoint(7, 0);
  private static final InventoryPosition KIT_BUTTON_CANCEL = InventoryPosition.ofPoint(7, 2);

  private final @NonNull GameMatch match;
  private final @NonNull GamePlayer viewer;

  public KitInventory(@NonNull GameMatch match, @NonNull GamePlayer viewer, @NonNull String title) {
    super(null, TickDuration.ofSecond(), DIMENSION_MIN, DIMENSION_MAX, new ArrayList<>(), title);
    Preconditions.checkNotNull(match, "Match must not be null");
    Preconditions.checkNotNull(viewer, "Viewer must not be null");
    this.match = match;
    this.viewer = viewer;
  }

  public void fillInventory() {
    KeyValueSet<String, SkywarsKit> kits = match.getKits();
    getElements().addAll(kits.stream()
        .map(KitItem::new)
        .collect(Collectors.toList()));
  }

  @Getter
  @RequiredArgsConstructor
  private final class KitItem implements InventoryItem {

    private static final int MAX_CONTENT_ROW_LENGTH = 4;

    private final @NonNull SkywarsKit kit;

    private final Supplier<InventoryItem> glassFactory =
        Suppliers.memoize(() -> InventoryItem.of(
            ItemBuilder.builder()
                .material(Material.GRAY_STAINED_GLASS_PANE)
                .name(StringUtils.SPACE)
                .build(),
            (whoClicked, e) -> e.setCancelled(true)));

    private final Supplier<InventoryItem> equipFactory =
        Suppliers.memoize(() -> InventoryItem.of(
            ItemBuilder.builder()
                .material(Material.LIME_CONCRETE)
                .name("§aEquip") // TODO
                .build(),
            (whoClicked, event) -> {
              event.setCancelled(true);
              viewer.getMatchData().setKit(getKit());
              viewer.sendMessage(Language.getInstance()
                  .get(MessageKeys.Match.KIT_SELECTION)
                  .substitute(whoClicked, ArrayPath.of("player")));
              viewer.getOnline().closeInventory();
              SoundRecord.ACTION_SUCCESS.play(whoClicked);
              // force scoreboard update
              match.getScoreboardHandlers()
                  .getHandler(MatchScoreboard.IDLE)
                  .findScoreboard(viewer)
                  .ifPresent(SpecialScoreboard::render);
            }));

    private final Supplier<InventoryItem> cancelFactory =
        Suppliers.memoize(() -> InventoryItem.of(
            ItemBuilder.builder()
                .material(Material.LIGHT_GRAY_CONCRETE)
                .name("§7Cancel") // TODO
                .build(),
            (whoClicked, event) -> {
              event.setCancelled(true);
              KitInventory.this.open(viewer.getOnline());
            }));

    @Override
    public ItemStack get(long ticks) {
      WrappedItemStack icon = kit.getIcon();
      if (icon != null) {
        ItemStack stack = icon.getStack();
        ItemMeta meta = stack.getItemMeta();
        if (stack.hasItemMeta() && meta != null && !meta.hasDisplayName()) {
          meta = meta.clone();
          stack = stack.clone();
          meta.setDisplayName(ChatColor.GRAY + kit.getName());
          stack.setItemMeta(meta);
        }
        return stack;
      }
      return ItemBuilder.builder()
          .name(kit.getName())
          .material(Material.CHEST)
          .build();
    }

    @Override
    public void click(GamePlayer player, InventoryClickEvent event) {
      Preconditions.checkState(player.equals(viewer));
      event.setCancelled(true);
      SpecialInventory<PaginatableInventoryContent> kitInventory =
          SpecialInventory.createInventory(TickDuration.ofSecond(), (inv) -> {
            // new paginatable inventory content will be modified further down
            return new PaginatableInventoryContent((x) -> inv.updateInventory(), KIT_DIMENSION);
          }, "Kit " + kit.getDisplayName());
      kitInventory.getOpenConnected().add(KitInventory.this);
      PaginatableInventoryContent content = kitInventory.getContent();
      int maxPerPage = (KIT_DIMENSION.getHeight() - 1) * MAX_CONTENT_ROW_LENGTH;
      List<List<WrappedItemStack>> itemLists = new ArrayList<>();
      List<WrappedItemStack> listBuilder = new ArrayList<>();
      Iterator<WrappedItemStack> iterator = kit.getContents().iterator();
      for (int cursor = 0; iterator.hasNext(); ++cursor) {
        listBuilder.add(iterator.next());
        if (cursor != 0 && cursor % (maxPerPage - 1) == 0) {
          itemLists.add(listBuilder);
          listBuilder = new ArrayList<>();
        }
      }
      if (!listBuilder.isEmpty())
        itemLists.add(listBuilder);
      final int pageSize = itemLists.size();
      for (List<WrappedItemStack> itemPage : itemLists)
        content.getPages().add(createKitPage(itemPage, pageSize != 1));
      content.setLastPagePos(PREVIOUS_PAGE);
      content.setNextPagePos(NEXT_PAGE);

      KitInventory.this.close(player.getOnline());
      kitInventory.open(player.getOnline());
    }

    InventoryPage createKitPage(List<WrappedItemStack> content, boolean hasPagination) {
      InventoryPage page = new InventoryPage(InventoryDimensions.ofRows(4));
      InventoryItem glass = glassFactory.get();
      page.fill(glass);
      InventoryItem equip = equipFactory.get();
      InventoryItem cancel = cancelFactory.get();
      createButton(KIT_BUTTON_EQUIP, equip, page);
      createButton(KIT_BUTTON_CANCEL, cancel, page);
      SkywarsKit.ArmorSlot[] values = SkywarsKit.ArmorSlot.values();
      for (int i = 0; i < values.length; ++i) {
        WrappedItemStack armor = kit.getArmor(values[i]);
        page.set(InventoryPosition.ofPoint(0, values.length - i - 1),
            armor == null ? null : InventoryItem.ofCancelling(armor));
      }
      InventoryPosition begin = InventoryPosition.ofPoint(2, 0);
      int maxReference = MAX_CONTENT_ROW_LENGTH - (hasPagination ? 1 : 0);
      for (int i = 0; i < content.size(); ++i)
        page.set(begin.add(i / maxReference, i % maxReference),
            InventoryItem.ofCancelling(content.get(i)));
      return page;
    }

    void createButton(InventoryPosition position, InventoryItem item, InventoryPage page) {
      for (int x = 0; x < 2; ++x) {
        for (int y = 0; y < 2; ++y) {
          page.set(position.add(x, y), item);
        }
      }
    }
  }

}
