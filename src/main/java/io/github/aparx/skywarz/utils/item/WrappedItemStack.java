package io.github.aparx.skywarz.utils.item;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.NumberConversions;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 05:34
 * @since 1.0
 */
@Getter
@SerializableAs("Skywarz.ItemWrapper")
public class WrappedItemStack implements ConfigurationSerializable {

  private final @NonNull ItemStack stack;

  public WrappedItemStack(@NonNull ItemStack item) {
    Preconditions.checkNotNull(item, "item must not be null");
    this.stack = item;
  }

  public static WrappedItemStack deserialize(Map<String, Object> data) {
    ItemStack item = new ItemStack(
        deserializeType(Objects.toString(data.get("type"))),
        Math.max(NumberConversions.toInt(data.get("amount")), 1));
    ItemMeta meta = item.getItemMeta();
    Preconditions.checkNotNull(meta);
    if (data.containsKey("name"))
      meta.setDisplayName((String) data.get("name"));
    if (data.containsKey("lore"))
      meta.setLore(((List<?>) data.get("lore")).stream()
          .map(Objects::toString)
          .collect(Collectors.toList()));
    if (data.containsKey("flags"))
      meta.addItemFlags(deserializeFlags((Collection<?>) data.get("flags")));
    if (data.containsKey("enchants")) {
      Object object = data.get("enchants");
      Map<Enchantment, Integer> deserialized = null;
      if (object instanceof MemorySection)
        deserialized = deserializeEnchantments(((MemorySection) object).getValues(false));
      else if (object instanceof Map<?, ?>)
        deserialized = deserializeEnchantments((Map<?, ?>) object);
      if (deserialized != null)
        deserialized.forEach((type, level) -> meta.addEnchant(type, level, true));
    }
    item.setItemMeta(meta);
    return new WrappedItemStack(item);
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> map = new LinkedHashMap<>();
    ItemMeta itemMeta = stack.getItemMeta();
    map.put("type", serializeType(stack.getType()));
    if (stack.getAmount() != 1)
      map.put("amount", stack.getAmount());
    if (itemMeta == null) return map;
    if (itemMeta.hasDisplayName())
      map.put("name", itemMeta.getDisplayName());
    if (itemMeta.hasEnchants())
      map.put("enchants", serializeEnchantments(itemMeta.getEnchants()));
    if (itemMeta.hasLore())
      map.put("lore", Optional.ofNullable(itemMeta.getLore()).orElseGet(ArrayList::new));
    if (!itemMeta.getItemFlags().isEmpty())
      map.put("flags", serializeFlags(itemMeta.getItemFlags()));
    return map;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WrappedItemStack that = (WrappedItemStack) o;
    return Objects.equals(stack, that.stack);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stack);
  }

  public WrappedItemStack copy() {
    return new WrappedItemStack(getStack().clone());
  }

  // DESERIALIZATION UTILITES

  private static Material deserializeType(String type) {
    return Optional.ofNullable(Material.matchMaterial(type)).orElseThrow(
        () -> new IllegalArgumentException(String.format("Material \"%s\" is unknown", type)));
  }

  private static String serializeType(Material material) {
    return material.name().toLowerCase().replace('_', ' ');
  }

  @SuppressWarnings("deprecation")
  private static Map<Enchantment, Integer> deserializeEnchantments(Map<?, ?> map) {
    Map<Enchantment, Integer> result = new HashMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      String key = Objects.toString(entry.getKey());
      int level = Integer.parseInt(String.valueOf(entry.getValue()));
      Enchantment enchantment = Enchantment.getByName(key);
      Preconditions.checkArgument(enchantment != null, "Enchantment not found", key);
      result.put(enchantment, level);
    }
    return result;
  }

  @SuppressWarnings("deprecation")
  private static Map<String, Integer> serializeEnchantments(Map<Enchantment, Integer> enchants) {
    if (enchants == null || enchants.isEmpty())
      return new HashMap<>();
    Map<String, Integer> map = new HashMap<>(enchants.size());
    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet())
      map.put(entry.getKey().getName(), entry.getValue());
    return map;
  }

  private static ItemFlag[] deserializeFlags(Collection<?> collection) {
    if (collection == null || collection.isEmpty())
      return new ItemFlag[0];
    return collection.stream()
        .map(Objects::toString)
        .map((string) -> deserializeEnum(ItemFlag.class, string))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toArray(ItemFlag[]::new);
  }

  private static List<String> serializeFlags(Collection<ItemFlag> flags) {
    if (flags == null || flags.isEmpty())
      return new ArrayList<>();
    List<String> list = new ArrayList<>(flags.size());
    for (ItemFlag flag : flags)
      list.add(serializeEnum(flag));
    return list;
  }

  private static <T extends Enum<T>> Optional<T> deserializeEnum(Class<T> type, String t) {
    try {
      return Optional.of(Enum.valueOf(type, t.replace(' ', '_').toUpperCase()));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private static <T extends Enum<T>> String serializeEnum(T t) {
    return t.name().toLowerCase().replace('_', ' ');
  }

}
