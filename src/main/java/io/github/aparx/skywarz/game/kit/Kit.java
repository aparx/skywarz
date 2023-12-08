package io.github.aparx.skywarz.game.kit;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.skywarz.utils.array.IndexMap;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 20:32
 * @since 1.0
 */
@Getter
@SerializableAs("Skywarz.Kit")
public final class Kit implements ConfigurationSerializable {

  // TODO Show selected kit in scoreboard

  private static final int MAX_STORAGE_LENGTH = 36;

  private final @NonNull String name;

  private final @Nullable WrappedItemStack icon;

  private final @Nullable WrappedItemStack @NonNull [] armor;
  private final @NonNull IndexMap<WrappedItemStack> contents;

  public Kit(@NonNull String name,
             @Nullable WrappedItemStack icon,
             @Nullable WrappedItemStack @NonNull [] armor,
             @NonNull IndexMap<WrappedItemStack> contents) {
    Preconditions.checkNotNull(name, "Name must not be null");
    Preconditions.checkNotNull(armor, "Armor array must not be null");
    Preconditions.checkNotNull(contents, "Contents array must not be null");
    Validate.notEmpty(name, "Name must not be empty");
    this.name = name;
    this.icon = icon;
    this.armor = armor;
    this.contents = contents;
  }

  public static KitBuilder builder(@NonNull String name) {
    return new KitBuilder(name);
  }

  public static Kit deserialize(Map<String, Object> args) {
    ArmorSlot[] values = ArmorSlot.values();
    WrappedItemStack[] armor = new WrappedItemStack[values.length];
    Object armorMap = args.get("armor");
    if (armorMap instanceof Map)
      for (ArmorSlot slot : values) {
        Object object = ((Map<?, ?>) armorMap).get(createArmorKey(slot));
        if (object instanceof WrappedItemStack)
          armor[slot.ordinal()] = (WrappedItemStack) object;
      }
    IndexMap<WrappedItemStack> contents = new IndexMap<>(0);
    Object contentMap = args.get("contents");
    if (contentMap instanceof Map)
      contents = deserializeContent((Map<?, ?>) contentMap);
    return new Kit((String) args.get("name"), (WrappedItemStack) args.get("icon"), armor, contents);
  }

  private static IndexMap<WrappedItemStack> deserializeContent(Map<?, ?> map) {
    IndexMap<WrappedItemStack> contents = new IndexMap<>(map.size());
    for (Map.Entry<?, ?> e : map.entrySet()) {
      int index = Integer.parseInt(Objects.toString(e.getKey(), null));
      Object value = e.getValue();
      if (value instanceof ItemStack)
        value = new WrappedItemStack((ItemStack) value);
      if (value instanceof WrappedItemStack)
        contents.put(index, (WrappedItemStack) value);
    }
    return contents;
  }

  @Override
  public @NonNull Map<String, Object> serialize() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("name", getName());
    map.put("icon", getIcon());
    Map<String, WrappedItemStack> armorMap = new LinkedHashMap<>();
    for (ArmorSlot slot : ArmorSlot.values())
      if (armor.length > slot.ordinal() && armor[slot.ordinal()] != null)
        armorMap.put(createArmorKey(slot), armor[slot.ordinal()]);
    map.put("armor", armorMap);
    map.put("contents", contents.toMap(LinkedHashMap::new));
    return map;
  }

  private static String createArmorKey(ArmorSlot slot) {
    return slot.name().toLowerCase();
  }

  public String getDisplayName() {
    if (icon == null) return getName();
    ItemStack stack = icon.getStack();
    ItemMeta itemMeta = stack.getItemMeta();
    if (!stack.hasItemMeta() || itemMeta == null || !itemMeta.hasDisplayName())
      return getName();
    return itemMeta.getDisplayName();
  }

  public void apply(@NonNull Player player) {
    PlayerInventory inventory = player.getInventory();
    inventory.clear();
    contents.forEach((item, index) -> {
      inventory.setItem(index, item.getStack());
    });
    inventory.setArmorContents(armor.length != 0
        ? Arrays.stream(armor)
        .filter(Objects::nonNull)
        .map(WrappedItemStack::getStack)
        .toArray(ItemStack[]::new)
        : null);
  }

  public @Nullable WrappedItemStack getArmor(@NonNull ArmorSlot slot) {
    Preconditions.checkNotNull(slot, "Slot must not be null");
    return armor.length > slot.ordinal() ? armor[slot.ordinal()] : null;
  }

  public Kit copy() {
    return new Kit(getName(), (icon != null ? icon.copy() : null),
        ArrayUtils.clone(armor), contents);
  }

  public enum ArmorSlot {
    BOOTS,
    LEGGINGS,
    CHESTPLATE,
    HELMET;
  }

  @Getter
  @Setter
  @Accessors(fluent = true)
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class KitBuilder {
    private final @NonNull String name;

    private final @Nullable WrappedItemStack @NonNull [] armor =
        new WrappedItemStack[ArmorSlot.values().length];

    private final IndexMap<WrappedItemStack> contents = new IndexMap<>();

    private WrappedItemStack icon;

    private int maxIndex = -1;

    @CanIgnoreReturnValue
    public KitBuilder slot(ArmorSlot slot, WrappedItemStack itemStack) {
      armor[slot.ordinal()] = itemStack;
      return this;
    }

    @CanIgnoreReturnValue
    public KitBuilder slot(ArmorSlot slot, ItemStack itemStack) {
      return this.slot(slot, new WrappedItemStack(itemStack));
    }

    @CanIgnoreReturnValue
    public KitBuilder slot(ArmorSlot slot, Material material) {
      return this.slot(slot, ItemBuilder.builder(material).build());
    }

    @CanIgnoreReturnValue
    public KitBuilder slot(@NonNegative int index, WrappedItemStack itemStack) {
      Preconditions.checkElementIndex(index, MAX_STORAGE_LENGTH);
      maxIndex = Math.max(index, maxIndex);
      contents.put(index, itemStack);
      return this;
    }

    @CanIgnoreReturnValue
    public KitBuilder slot(@NonNegative int index, ItemStack itemStack) {
      return this.slot(index, new WrappedItemStack(itemStack));
    }

    @CanIgnoreReturnValue
    public KitBuilder slot(@NonNegative int index, Material material) {
      return this.slot(index, ItemBuilder.builder(material).wrap());
    }

    @CanIgnoreReturnValue
    public KitBuilder add(WrappedItemStack itemStack) {
      return this.slot(++maxIndex, itemStack);
    }

    @CanIgnoreReturnValue
    public KitBuilder add(ItemStack itemStack) {
      return this.slot(++maxIndex, new WrappedItemStack(itemStack));
    }

    @CanIgnoreReturnValue
    public KitBuilder add(Material material) {
      return add(ItemBuilder.builder(material).wrap());
    }

    @CanIgnoreReturnValue
    public KitBuilder repeatAdd(int repeat, WrappedItemStack itemStack) {
      while (--repeat >= 0) add(itemStack);
      return this;
    }

    @CanIgnoreReturnValue
    public KitBuilder repeatAdd(int repeat, Material material) {
      return repeatAdd(repeat, ItemBuilder.builder(material).wrap());
    }

    @CheckReturnValue
    public @NonNull Kit build() {
      return new Kit(name, icon, ArrayUtils.clone(armor), new IndexMap<>(contents));
    }

  }

}
