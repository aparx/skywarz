package io.github.aparx.skywarz.game.chest;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.skywarz.Skywars;
import lombok.Getter;
import org.bukkit.Material;

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


  private static final int WOOD = 25;
  private static final int GOLDEN = 15;
  private static final int STONE = 13;
  private static final int IRON = 10;
  private static final int DIAMOND = 5;

  private static final int ARMOR_OFFSET = 5;

  private static final int HALF_AMOUNT = 32;

  private static final double BLOCK_MULTIPLIER = 1.5;
  private static final double FOOD_MULTIPLIER = 1.25;

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
      "    ARROW PROTECTION: 2"
  })
  private ChestItems items = new ChestItems();

  private ChestConfig() {
    super((proxy) -> Skywars.getInstance().getConfigHandler().getOrCreate("chests"));
    items.addAll(getTiers());
    items.addAll(getBlocks());
    items.addAll(getFood());
    items.addAll(Set.of(
        new ChestItem(Material.ENDER_PEARL, DIAMOND),
        new ChestItem(Material.LAVA_BUCKET, IRON - 2),
        new ChestItem(Material.WATER_BUCKET, IRON),
        new ChestItem(Material.STICK, WOOD)
    ));
  }

  private Set<ChestItem> getTiers() {
    return Set.of(
        /* WOOD TIER */
        new ChestItem(Material.LEATHER_BOOTS, ARMOR_OFFSET + WOOD),
        new ChestItem(Material.LEATHER_CHESTPLATE, WOOD),
        new ChestItem(Material.LEATHER_LEGGINGS, ARMOR_OFFSET + WOOD),
        new ChestItem(Material.LEATHER_HELMET, ARMOR_OFFSET + WOOD),
        new ChestItem(Material.WOODEN_SWORD, WOOD),
        /* STONE TIER */
        new ChestItem(Material.CHAINMAIL_BOOTS, ARMOR_OFFSET + STONE),
        new ChestItem(Material.CHAINMAIL_CHESTPLATE, STONE),
        new ChestItem(Material.CHAINMAIL_LEGGINGS, ARMOR_OFFSET + STONE),
        new ChestItem(Material.CHAINMAIL_HELMET, ARMOR_OFFSET + STONE),
        new ChestItem(Material.STONE_SWORD, STONE),
        new ChestItem(Material.STONE, HALF_AMOUNT, BLOCK_MULTIPLIER * DIAMOND),
        /* GOLDEN TIER */
        new ChestItem(Material.GOLDEN_BOOTS, ARMOR_OFFSET + GOLDEN),
        new ChestItem(Material.GOLDEN_CHESTPLATE, GOLDEN),
        new ChestItem(Material.GOLDEN_LEGGINGS, ARMOR_OFFSET + GOLDEN),
        new ChestItem(Material.GOLDEN_HELMET, ARMOR_OFFSET + GOLDEN),
        new ChestItem(Material.GOLDEN_SWORD, GOLDEN),
        new ChestItem(Material.GOLD_INGOT, 8, BLOCK_MULTIPLIER * GOLDEN / 2),
        new ChestItem(Material.GOLD_BLOCK, 1, BLOCK_MULTIPLIER * GOLDEN / 3),
        /* IRON TIER */
        new ChestItem(Material.IRON_BOOTS, ARMOR_OFFSET + IRON),
        new ChestItem(Material.IRON_CHESTPLATE, IRON),
        new ChestItem(Material.IRON_LEGGINGS, ARMOR_OFFSET + IRON),
        new ChestItem(Material.IRON_HELMET, ARMOR_OFFSET + IRON),
        new ChestItem(Material.IRON_SWORD, IRON),
        new ChestItem(Material.IRON_INGOT, 8, BLOCK_MULTIPLIER * IRON / 2),
        new ChestItem(Material.IRON_BLOCK, 1, BLOCK_MULTIPLIER * IRON / 3),
        /* DIAMOND TIER */
        new ChestItem(Material.DIAMOND_BOOTS, ARMOR_OFFSET + DIAMOND),
        new ChestItem(Material.DIAMOND_CHESTPLATE, DIAMOND),
        new ChestItem(Material.DIAMOND_LEGGINGS, ARMOR_OFFSET + DIAMOND),
        new ChestItem(Material.DIAMOND_HELMET, ARMOR_OFFSET + DIAMOND),
        new ChestItem(Material.DIAMOND_SWORD, DIAMOND),
        new ChestItem(Material.DIAMOND, 8, BLOCK_MULTIPLIER * DIAMOND / 2),
        new ChestItem(Material.DIAMOND_BLOCK, 1, BLOCK_MULTIPLIER * DIAMOND / 3)
    );
  }

  private Set<ChestItem> getBlocks() {
    return Set.of(
        new ChestItem(Material.DIRT, HALF_AMOUNT, BLOCK_MULTIPLIER * WOOD),
        new ChestItem(Material.GRASS_BLOCK, HALF_AMOUNT, BLOCK_MULTIPLIER * WOOD),
        new ChestItem(Material.ACACIA_WOOD, HALF_AMOUNT, BLOCK_MULTIPLIER * WOOD),
        new ChestItem(Material.BIRCH_WOOD, HALF_AMOUNT, BLOCK_MULTIPLIER * WOOD),
        new ChestItem(Material.ACACIA_PLANKS, HALF_AMOUNT, BLOCK_MULTIPLIER * WOOD),
        new ChestItem(Material.BIRCH_PLANKS, HALF_AMOUNT, BLOCK_MULTIPLIER * WOOD)
    );
  }

  private Set<ChestItem> getFood() {
    return Set.of(
        new ChestItem(Material.APPLE, HALF_AMOUNT, FOOD_MULTIPLIER * WOOD),
        new ChestItem(Material.MELON, HALF_AMOUNT, FOOD_MULTIPLIER * DIAMOND),
        new ChestItem(Material.BAKED_POTATO, HALF_AMOUNT / 2, FOOD_MULTIPLIER * DIAMOND),
        new ChestItem(Material.BEEF, HALF_AMOUNT / 2, FOOD_MULTIPLIER * DIAMOND),
        new ChestItem(Material.COOKED_BEEF, HALF_AMOUNT / 3, FOOD_MULTIPLIER * DIAMOND),
        new ChestItem(Material.CARROT, HALF_AMOUNT, FOOD_MULTIPLIER * DIAMOND),
        new ChestItem(Material.GOLDEN_CARROT, HALF_AMOUNT / 2, FOOD_MULTIPLIER * DIAMOND),
        new ChestItem(Material.GOLDEN_APPLE, 3, FOOD_MULTIPLIER * DIAMOND)
    );
  }

}
