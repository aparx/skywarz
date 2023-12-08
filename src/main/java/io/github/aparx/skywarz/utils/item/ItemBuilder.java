package io.github.aparx.skywarz.utils.item;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 06:12
 * @since 1.0
 */
@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemBuilder {

  private @Nullable Map<Enchantment, Integer> enchants = new HashMap<>();

  private @NonNull Material material = Material.AIR;
  private @NonNegative int amount;
  private @Nullable String name;
  private @Nullable List<String> lore;

  @Setter(AccessLevel.NONE)
  private @Nullable ItemFlag[] flags;

  @Setter(AccessLevel.NONE)
  private @NonNull List<Consumer<ItemMeta>> modifiers = new ArrayList<>();

  public static ItemBuilder builder() {
    return new ItemBuilder();
  }

  public static ItemBuilder builder(@NonNull Material material) {
    return new ItemBuilder().material(material);
  }

  public static ItemBuilder builder(@NonNull Material material, @NonNegative int amount) {
    return new ItemBuilder().material(material).amount(amount);
  }

  @CanIgnoreReturnValue
  public ItemBuilder material(@NonNull Material material) {
    Preconditions.checkNotNull(material, "Material must not be null");
    this.material = material;
    return this;
  }

  @CanIgnoreReturnValue
  public ItemBuilder amount(int amount) {
    Preconditions.checkArgument(amount >= 0, "Amount must be positive");
    this.amount = amount;
    return this;
  }

  @CanIgnoreReturnValue
  public ItemBuilder lore(List<String> lore) {
    this.lore = lore;
    return this;
  }

  @CanIgnoreReturnValue
  public ItemBuilder lore(String first, String... successors) {
    this.lore = new ArrayList<>();
    lore.add(first);
    Arrays.stream(successors)
        .filter(Objects::nonNull)
        .forEach(lore::add);
    return this;
  }

  @CanIgnoreReturnValue
  public ItemBuilder flags(ItemFlag first, ItemFlag... successors) {
    flags = (ItemFlag[]) ArrayUtils.add(successors, 0, first);
    return this;
  }

  @CanIgnoreReturnValue
  public ItemBuilder flags(ItemFlag[] flags) {
    this.flags = flags;
    return this;
  }

  @CanIgnoreReturnValue
  public ItemBuilder modify(@NonNull Consumer<ItemMeta> modifier) {
    Preconditions.checkNotNull(modifier, "Modifier must not be null");
    modifiers.add(modifier);
    return this;
  }

  public WrappedItemStack wrap() {
    return new WrappedItemStack(build());
  }

  public ItemStack build() {
    ItemStack stack = new ItemStack(material, Math.max(amount, 1));
    ItemMeta itemMeta = stack.getItemMeta();
    Preconditions.checkNotNull(itemMeta);
    itemMeta.setLore(lore);
    itemMeta.setDisplayName(name);
    if (ArrayUtils.isNotEmpty(flags))
      itemMeta.addItemFlags(Arrays.stream(flags)
          .filter(Objects::nonNull)
          .toArray(ItemFlag[]::new));
    if (enchants != null)
      enchants.forEach((type, level) -> itemMeta.addEnchant(type, level, true));
    modifiers.forEach((modifier) -> modifier.accept(itemMeta));
    stack.setItemMeta(itemMeta);
    return stack;
  }

}
