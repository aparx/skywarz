package io.github.aparx.skywarz.game.chest;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.Set;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 13:25
 * @since 1.0
 */
@Getter
public final class ChestConfig extends ConfigObject {

  @Getter
  private static final ChestConfig instance = new ChestConfig();

  // P.S: Could you guess after what game these constants are named?
  private static final int COMMON = 25;
  private static final int UNCOMMON = 15;
  private static final int RARE = 13;
  private static final int EPIC = 10;
  private static final int LEGENDARY = 5;

  private static final int ARMOR_OFFSET = 6;

  private static final int HALF_AMOUNT = 32;

  private static final double BLOCK_MULTIPLIER = 1.75;
  private static final double FOOD_MULTIPLIER = 1.5;
  private static final double TOOL_MULTIPLIER = 1.25;

  @ConfigMapping("fill slot probability")
  @Document({
      "The probability that a specific slot is being set an item to within a chest.",
      "This value should be between 0 and 100 (Default: 35)."
  })
  private int probability = 35;

  @ConfigMapping
  @Document({
      "A list of all items possibly occurring in chests.",
      "You can add enchantments by defining an \"enchants\" section as in this example:",
      "- ==: Skywarz.ChestItem",
      "  weight: 10.0",
      "  type: diamond chestplate",
      "  enchants:",
      "    PROTECTION_FIRE: 2",
      "Enchantments names are done after Bukkit's enchantment enumeration:",
      "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html"
  })
  private ChestItems items = new ChestItems();

  private ChestConfig() {
    super((proxy) -> Skywars.getInstance().getConfigHandler().getOrCreate("chest"));
    items.addAll(getTiers());
    items.addAll(getBlocks());
    items.addAll(getFood());
    items.addAll(Set.of(
        new ChestItem(Material.ENDER_PEARL, LEGENDARY),
        new ChestItem(Material.LAVA_BUCKET, EPIC - 2),
        new ChestItem(Material.WATER_BUCKET, EPIC),
        new ChestItem(Material.STICK, COMMON),
        new ChestItem(Material.FLINT_AND_STEEL, UNCOMMON)
    ));
  }

  @Override
  public void save() {
    setHeaderIfAbsent(SkywarsConfigHandler.createHeader(
        "Chest configuration",
        "Edit the items and their possibility of appearing in chests!",
        "The higher the weight, the higher the chance the item is put into a chest."
    ));
    super.save();
  }

  private Set<ChestItem> getTiers() {
    return Set.of(
        /* WOOD TIER */
        new ChestItem(Material.LEATHER_BOOTS, ARMOR_OFFSET + COMMON),
        new ChestItem(Material.LEATHER_CHESTPLATE, COMMON),
        new ChestItem(Material.LEATHER_LEGGINGS, ARMOR_OFFSET + COMMON),
        new ChestItem(Material.LEATHER_HELMET, ARMOR_OFFSET + COMMON),
        new ChestItem(Material.WOODEN_SWORD, COMMON),
        new ChestItem(Material.WOODEN_AXE, TOOL_MULTIPLIER * COMMON),
        new ChestItem(Material.WOODEN_SHOVEL, TOOL_MULTIPLIER * COMMON),
        new ChestItem(Material.WOODEN_PICKAXE, TOOL_MULTIPLIER * COMMON),
        /* GOLDEN TIER */
        new ChestItem(Material.GOLDEN_BOOTS, ARMOR_OFFSET + UNCOMMON),
        new ChestItem(Material.GOLDEN_CHESTPLATE, UNCOMMON),
        new ChestItem(Material.GOLDEN_LEGGINGS, ARMOR_OFFSET + UNCOMMON),
        new ChestItem(Material.GOLDEN_HELMET, ARMOR_OFFSET + UNCOMMON),
        new ChestItem(Material.GOLDEN_SWORD, UNCOMMON),
        new ChestItem(Material.GOLDEN_AXE, TOOL_MULTIPLIER * UNCOMMON),
        new ChestItem(Material.GOLDEN_SHOVEL, TOOL_MULTIPLIER * UNCOMMON),
        new ChestItem(Material.GOLDEN_PICKAXE, TOOL_MULTIPLIER * UNCOMMON),
        new ChestItem(Material.GOLD_INGOT, 8, BLOCK_MULTIPLIER * UNCOMMON / 2),
        new ChestItem(Material.GOLD_BLOCK, 1, BLOCK_MULTIPLIER * UNCOMMON / 3),
        /* STONE TIER */
        new ChestItem(Material.CHAINMAIL_BOOTS, ARMOR_OFFSET + RARE),
        new ChestItem(Material.CHAINMAIL_CHESTPLATE, RARE),
        new ChestItem(Material.CHAINMAIL_LEGGINGS, ARMOR_OFFSET + RARE),
        new ChestItem(Material.CHAINMAIL_HELMET, ARMOR_OFFSET + RARE),
        new ChestItem(Material.STONE_SWORD, RARE),
        new ChestItem(Material.STONE_AXE, TOOL_MULTIPLIER * RARE),
        new ChestItem(Material.STONE_SHOVEL, TOOL_MULTIPLIER * RARE),
        new ChestItem(Material.STONE_PICKAXE, TOOL_MULTIPLIER * RARE),
        new ChestItem(Material.STONE, HALF_AMOUNT, BLOCK_MULTIPLIER * UNCOMMON),
        /* IRON TIER */
        new ChestItem(Material.IRON_BOOTS, ARMOR_OFFSET + EPIC),
        new ChestItem(Material.IRON_CHESTPLATE, EPIC),
        new ChestItem(Material.IRON_LEGGINGS, ARMOR_OFFSET + EPIC),
        new ChestItem(Material.IRON_HELMET, ARMOR_OFFSET + EPIC),
        new ChestItem(Material.IRON_SWORD, EPIC),
        new ChestItem(Material.IRON_AXE, TOOL_MULTIPLIER * EPIC),
        new ChestItem(Material.IRON_SHOVEL, TOOL_MULTIPLIER * EPIC),
        new ChestItem(Material.IRON_PICKAXE, TOOL_MULTIPLIER * EPIC),
        new ChestItem(Material.IRON_INGOT, 8, BLOCK_MULTIPLIER * EPIC / 2),
        new ChestItem(Material.IRON_BLOCK, 1, BLOCK_MULTIPLIER * EPIC / 3),
        /* DIAMOND TIER */
        new ChestItem(Material.DIAMOND_BOOTS, ARMOR_OFFSET + LEGENDARY),
        new ChestItem(Material.DIAMOND_CHESTPLATE, LEGENDARY),
        new ChestItem(Material.DIAMOND_LEGGINGS, ARMOR_OFFSET + LEGENDARY),
        new ChestItem(Material.DIAMOND_HELMET, ARMOR_OFFSET + LEGENDARY),
        new ChestItem(Material.DIAMOND_SWORD, LEGENDARY),
        new ChestItem(Material.DIAMOND_AXE, TOOL_MULTIPLIER * LEGENDARY),
        new ChestItem(Material.DIAMOND_SHOVEL, TOOL_MULTIPLIER * LEGENDARY),
        new ChestItem(Material.DIAMOND_PICKAXE, TOOL_MULTIPLIER * LEGENDARY),
        new ChestItem(Material.DIAMOND, 8, BLOCK_MULTIPLIER * LEGENDARY / 2)
    );
  }

  private Set<ChestItem> getBlocks() {
    return Set.of(
        new ChestItem(Material.COBWEB, UNCOMMON),
        new ChestItem(Material.DIRT, HALF_AMOUNT, BLOCK_MULTIPLIER * COMMON),
        new ChestItem(Material.GRASS_BLOCK, HALF_AMOUNT, BLOCK_MULTIPLIER * COMMON),
        new ChestItem(Material.ACACIA_WOOD, HALF_AMOUNT, BLOCK_MULTIPLIER * COMMON),
        new ChestItem(Material.BIRCH_WOOD, HALF_AMOUNT, BLOCK_MULTIPLIER * COMMON),
        new ChestItem(Material.ACACIA_PLANKS, HALF_AMOUNT, BLOCK_MULTIPLIER * COMMON),
        new ChestItem(Material.BIRCH_PLANKS, HALF_AMOUNT, BLOCK_MULTIPLIER * COMMON)
    );
  }

  private Set<ChestItem> getFood() {
    return Set.of(
        new ChestItem(Material.APPLE, HALF_AMOUNT, FOOD_MULTIPLIER * COMMON),
        new ChestItem(Material.MELON_SLICE, HALF_AMOUNT, FOOD_MULTIPLIER * RARE),
        new ChestItem(Material.BAKED_POTATO, HALF_AMOUNT / 2, FOOD_MULTIPLIER * RARE),
        new ChestItem(Material.BEEF, HALF_AMOUNT / 2, FOOD_MULTIPLIER * EPIC),
        new ChestItem(Material.COOKED_BEEF, HALF_AMOUNT / 3, FOOD_MULTIPLIER * EPIC),
        new ChestItem(Material.CARROT, HALF_AMOUNT, FOOD_MULTIPLIER * UNCOMMON),
        new ChestItem(Material.GOLDEN_CARROT, HALF_AMOUNT / 2, FOOD_MULTIPLIER * LEGENDARY),
        new ChestItem(Material.GOLDEN_APPLE, 3, FOOD_MULTIPLIER * LEGENDARY)
    );
  }

}
