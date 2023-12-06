package io.github.aparx.skywarz.game.chest;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 12:00
 * @since 1.0
 */
@Getter
@SerializableAs("Skywarz.ChestItem")
public final class ChestItem extends WrappedItemStack implements ConfigurationSerializable {

  /** Weight of this item actually occurring */
  private final double weight;

  public ChestItem(@NonNull Material material, int amount, double weight) {
    this(ItemBuilder.builder(material).amount(amount).build(), weight);
  }

  public ChestItem(@NonNull Material material, double weight) {
    this(ItemBuilder.builder(material).build(), weight);
  }

  public ChestItem(@NonNull ItemStack stack, double weight) {
    super(stack);
    Preconditions.checkArgument(weight >= 0 && weight <= 100,
        "Weight must be between 0 and 100");
    this.weight = weight;
  }

  @CheckReturnValue
  public static ChestItem deserialize(Map<String, Object> data) {
    return new ChestItem(WrappedItemStack.deserialize(data).getStack(),
        NumberConversions.toDouble(data.get("weight")));
  }

  @Override
  public @NonNull Map<String, Object> serialize() {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("weight", weight);
    map.putAll(super.serialize());
    return map;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    ChestItem item = (ChestItem) o;
    return Double.compare(weight, item.weight) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), weight);
  }
}
