package io.github.aparx.skywarz.game.kit;

import io.github.aparx.skywarz.utils.item.ItemBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 22:37
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum DefaultKits {
  RUSHER(SkywarsKit.builder("Rusher")
      .icon(ItemBuilder.builder()
          .name("§c§lRusher")
          .material(Material.IRON_SWORD)
          .enchants(Map.of(Enchantment.LUCK, 2))
          .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
          .wrap())
      .slot(SkywarsKit.ArmorSlot.HELMET, ItemBuilder.builder(Material.IRON_HELMET).wrap())
      .slot(SkywarsKit.ArmorSlot.CHESTPLATE, ItemBuilder.builder(Material.DIAMOND_CHESTPLATE).wrap())
      .slot(SkywarsKit.ArmorSlot.LEGGINGS, ItemBuilder.builder(Material.IRON_LEGGINGS).wrap())
      .slot(SkywarsKit.ArmorSlot.BOOTS, ItemBuilder.builder(Material.LEATHER_BOOTS).wrap())
      .add(ItemBuilder.builder(Material.IRON_SWORD).wrap())
      .add(ItemBuilder.builder(Material.STONE).amount(32).wrap())
      .slot(8, ItemBuilder.builder(Material.ENCHANTED_GOLDEN_APPLE).amount(3).wrap())
      .build()),

  BUILDER(SkywarsKit.builder("Builder")
      .icon(ItemBuilder.builder()
          .name("§9§lBuilder")
          .material(Material.WOODEN_AXE)
          .enchants(Map.of(Enchantment.LUCK, 2))
          .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
          .wrap())
      .slot(SkywarsKit.ArmorSlot.HELMET, Material.DIAMOND_HELMET)
      .slot(SkywarsKit.ArmorSlot.CHESTPLATE, Material.CHAINMAIL_CHESTPLATE)
      .slot(SkywarsKit.ArmorSlot.LEGGINGS, Material.LEATHER_LEGGINGS)
      .slot(SkywarsKit.ArmorSlot.BOOTS, Material.LEATHER_BOOTS)
      .add(Material.IRON_SWORD)
      .add(Material.IRON_AXE)
      .add(Material.IRON_PICKAXE)
      .add(ItemBuilder.builder(Material.STONE).amount(64).wrap())
      .add(ItemBuilder.builder(Material.DIRT).amount(32).wrap())
      .add(ItemBuilder.builder(Material.SPRUCE_PLANKS).amount(32).wrap())
      .slot(7, ItemBuilder.builder(Material.BAKED_POTATO).amount(8).wrap())
      .slot(8, ItemBuilder.builder(Material.GOLDEN_APPLE).amount(2).wrap())
      .build()),

  DIRT(SkywarsKit.builder("Dirt")
      .icon(ItemBuilder.builder()
          .name(ChatColor.RED + "Dirt")
          .lore(ChatColor.GRAY + "All wood, all dirt")
          .material(Material.DIRT)
          .wrap())
      .slot(SkywarsKit.ArmorSlot.HELMET, ItemBuilder.builder(Material.LEATHER_HELMET).wrap())
      .slot(SkywarsKit.ArmorSlot.CHESTPLATE, ItemBuilder.builder(Material.LEATHER_CHESTPLATE).wrap())
      .slot(SkywarsKit.ArmorSlot.LEGGINGS, ItemBuilder.builder(Material.LEATHER_LEGGINGS).wrap())
      .slot(SkywarsKit.ArmorSlot.BOOTS, ItemBuilder.builder(Material.LEATHER_BOOTS).wrap())
      .add(Material.WOODEN_SWORD)
      .add(Material.WOODEN_AXE)
      .add(Material.WOODEN_PICKAXE)
      .add(ItemBuilder.builder(Material.ENCHANTED_GOLDEN_APPLE).amount(5).wrap())
      .add(ItemBuilder.builder(Material.BIRCH_PLANKS).amount(32).wrap())
      .add(ItemBuilder.builder(Material.JUNGLE_PLANKS).amount(32).wrap())
      .add(ItemBuilder.builder(Material.OAK_PLANKS).amount(32).wrap())
      .repeatAdd(6, ItemBuilder.builder(Material.DIRT).amount(32).wrap())
      .build());

  private final @NonNull SkywarsKit kit;
}
