package io.github.aparx.skywarz.handler.configs;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.bufig.configurable.object.ConfigId;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.bufig.handler.ConfigHandler;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.ItemWrapper;
import lombok.Getter;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 03:37
 * @since 1.0
 */
@Getter
@ConfigId("items")
public class ItemConfig extends ConfigObject {

  @ConfigMapping
  @Document("The team selector is what a player uses to select their team.")
  private ItemWrapper teamSelector = ItemBuilder
      .builder(Material.WHITE_WOOL)
      .lore("§8Click to select your team")
      .name("Team selector")
      .enchants(Map.of(Enchantment.LUCK, 2))
      .wrap();

  @ConfigMapping
  @Document("Item used to leave a match")
  private ItemWrapper leave = ItemBuilder
      .builder(Material.RED_DYE)
      .lore("§8Click to leave this match")
      .name("§cLeave")
      .enchants(Map.of(Enchantment.LUCK, 2))
      .flags(ItemFlag.HIDE_ENCHANTS)
      .wrap();

  public ItemConfig() {
    this(Skywars.getInstance().getConfigHandler());
  }

  public ItemConfig(@NonNull ConfigHandler<?> handler) {
    super(handler);
  }

  @Override
  public void save() {
    setHeader(SkywarsConfigHandler.createHeader(
        "Modify generic items, such as lobby items"
    ));
    super.save();
  }
}
