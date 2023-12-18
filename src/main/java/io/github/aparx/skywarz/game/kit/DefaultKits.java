package io.github.aparx.skywarz.game.kit;

import io.github.aparx.skywarz.utils.item.ItemBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.qual.C;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 22:37
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum DefaultKits {
  DIRT(GameKit.builder("Dirt")
      .icon(ItemBuilder.builder()
          .name(ChatColor.RED + "Dirt")
          .lore(ChatColor.GRAY + "All wood, all dirt")
          .material(Material.DIRT)
          .wrap())
      .slot(GameKit.ArmorSlot.HELMET, ItemBuilder.builder(Material.LEATHER_HELMET).wrap())
      .slot(GameKit.ArmorSlot.CHESTPLATE, ItemBuilder.builder(Material.LEATHER_CHESTPLATE).wrap())
      .slot(GameKit.ArmorSlot.LEGGINGS, ItemBuilder.builder(Material.LEATHER_LEGGINGS).wrap())
      .slot(GameKit.ArmorSlot.BOOTS, ItemBuilder.builder(Material.LEATHER_BOOTS).wrap())
      .add(Material.WOODEN_AXE)
      .add(Material.WOODEN_PICKAXE)
      .add(ItemBuilder.builder(Material.BIRCH_PLANKS).amount(32).wrap())
      .add(ItemBuilder.builder(Material.JUNGLE_PLANKS).amount(32).wrap())
      .add(ItemBuilder.builder(Material.OAK_PLANKS).amount(32).wrap())
      .repeatAdd(4, ItemBuilder.builder(Material.DIRT).amount(32).wrap())
      .build()),

  RUSHER(GameKit.builder("Rusher")
      .icon(ItemBuilder.builder()
          .name(ChatColor.RED + "Rusher")
          .material(Material.IRON_SWORD)
          .enchants(Map.of(Enchantment.LUCK, 2))
          .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
          .wrap())
      .slot(GameKit.ArmorSlot.HELMET, ItemBuilder.builder(Material.IRON_HELMET).wrap())
      .slot(GameKit.ArmorSlot.CHESTPLATE, ItemBuilder.builder(Material.DIAMOND_CHESTPLATE).wrap())
      .slot(GameKit.ArmorSlot.LEGGINGS, ItemBuilder.builder(Material.IRON_LEGGINGS).wrap())
      .slot(GameKit.ArmorSlot.BOOTS, ItemBuilder.builder(Material.LEATHER_BOOTS).wrap())
      .add(ItemBuilder.builder(Material.IRON_SWORD).wrap())
      .add(ItemBuilder.builder(Material.STONE).amount(32).wrap())
      .slot(8, ItemBuilder.builder(Material.ENCHANTED_GOLDEN_APPLE).amount(3).wrap())
      .build()),

  BUILDER(GameKit.builder("Builder")
      .icon(ItemBuilder.builder()
          .name(ChatColor.BLUE + "Builder")
          .material(Material.WOODEN_AXE)
          .enchants(Map.of(Enchantment.LUCK, 2))
          .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
          .wrap())
      .slot(GameKit.ArmorSlot.HELMET, Material.DIAMOND_HELMET)
      .slot(GameKit.ArmorSlot.CHESTPLATE, Material.CHAINMAIL_CHESTPLATE)
      .slot(GameKit.ArmorSlot.LEGGINGS, Material.LEATHER_LEGGINGS)
      .slot(GameKit.ArmorSlot.BOOTS, Material.LEATHER_BOOTS)
      .add(Material.IRON_SWORD)
      .add(Material.IRON_AXE)
      .add(Material.IRON_PICKAXE)
      .add(ItemBuilder.builder(Material.STONE).amount(64).wrap())
      .add(ItemBuilder.builder(Material.DIRT).amount(32).wrap())
      .add(ItemBuilder.builder(Material.SPRUCE_PLANKS).amount(32).wrap())
      .slot(7, ItemBuilder.builder(Material.BAKED_POTATO).amount(8).wrap())
      .slot(8, ItemBuilder.builder(Material.GOLDEN_APPLE).amount(2).wrap())
      .build()),

  DIAMOND(GameKit.builder("Diamond")
      .icon(ItemBuilder.builder()
          .name(ChatColor.AQUA + "Diamond")
          .material(Material.DIAMOND)
          .wrap())
      .slot(GameKit.ArmorSlot.CHESTPLATE, Material.LEATHER_CHESTPLATE)
      .slot(GameKit.ArmorSlot.BOOTS, Material.LEATHER_BOOTS)
      .add(Material.DIAMOND_SWORD)
      .add(Material.DIAMOND_AXE)
      .add(Material.DIAMOND_SHOVEL)
      .add(ItemBuilder.builder(Material.DIRT).amount(32).wrap())
      .slot(7, ItemBuilder.builder(Material.GOLDEN_APPLE).amount(2).wrap())
      .slot(8, ItemBuilder.builder(Material.BEEF).amount(8).wrap())
      .build()),

  IRON(GameKit.builder("Iron")
      .icon(ItemBuilder.builder()
          .name(ChatColor.GRAY + "Iron")
          .material(Material.IRON_INGOT)
          .wrap())
      .slot(GameKit.ArmorSlot.HELMET, Material.IRON_HELMET)
      .slot(GameKit.ArmorSlot.CHESTPLATE, Material.LEATHER_CHESTPLATE)
      .slot(GameKit.ArmorSlot.LEGGINGS, Material.CHAINMAIL_LEGGINGS)
      .slot(GameKit.ArmorSlot.BOOTS, Material.LEATHER_BOOTS)
      .add(Material.IRON_SWORD)
      .add(Material.IRON_AXE)
      .add(Material.IRON_SHOVEL)
      .add(ItemBuilder.builder(Material.DIRT).amount(32).wrap())
      .slot(6, ItemBuilder.builder(Material.IRON_INGOT).amount(2).wrap())
      .slot(7, ItemBuilder.builder(Material.GOLDEN_APPLE).amount(2).wrap())
      .slot(8, ItemBuilder.builder(Material.BEEF).amount(8).wrap())
      .build()),

  GOLD(GameKit.builder("Gold")
      .icon(ItemBuilder.builder()
          .name(ChatColor.GOLD + "Gold")
          .material(Material.GOLD_INGOT)
          .wrap())
      .slot(GameKit.ArmorSlot.CHESTPLATE, Material.GOLDEN_CHESTPLATE)
      .slot(GameKit.ArmorSlot.BOOTS, Material.GOLDEN_BOOTS)
      .add(Material.GOLDEN_SWORD)
      .add(Material.GOLDEN_AXE)
      .add(Material.GOLDEN_SHOVEL)
      .add(ItemBuilder.builder(Material.DIRT).amount(32).wrap())
      .add(ItemBuilder.builder(Material.DIRT).amount(32).wrap())
      .slot(6, ItemBuilder.builder(Material.GOLD_INGOT).amount(6).wrap())
      .slot(7, ItemBuilder.builder(Material.GOLDEN_APPLE).amount(4).wrap())
      .slot(8, ItemBuilder.builder(Material.BEEF).amount(16).wrap())
      .build()),

  STONE(GameKit.builder("Stone")
      .icon(ItemBuilder.builder()
          .name(ChatColor.GRAY + "Stone")
          .material(Material.STONE)
          .wrap())
      .slot(GameKit.ArmorSlot.CHESTPLATE, Material.CHAINMAIL_CHESTPLATE)
      .slot(GameKit.ArmorSlot.BOOTS, Material.CHAINMAIL_BOOTS)
      .add(Material.STONE_SWORD)
      .add(Material.STONE_AXE)
      .add(Material.STONE_SHOVEL)
      .add(ItemBuilder.builder(Material.DIRT).amount(32).wrap())
      .add(ItemBuilder.builder(Material.DIRT).amount(32).wrap())
      .add(ItemBuilder.builder(Material.STONE).amount(32).wrap())
      .slot(6, ItemBuilder.builder(Material.GOLD_INGOT).amount(6).wrap())
      .slot(7, ItemBuilder.builder(Material.ENCHANTED_GOLDEN_APPLE).amount(4).wrap())
      .slot(8, ItemBuilder.builder(Material.BEEF).amount(32).wrap())
      .build()),

  ;

  private final @NonNull GameKit kit;
}
