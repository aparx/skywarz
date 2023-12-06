package io.github.aparx.skywarz.utils.item;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 12:13
 * @since 1.0
 */
@Getter
@SerializableAs("Skywarz.SkullItem")
public final class SkullItem extends WrappedItemStack implements ConfigurationSerializable {

  public SkullItem(@NonNull ItemStack stack) {
    super(stack);
    stack.setType(Material.PLAYER_HEAD);
  }

  public static SkullItem of(@NonNull ItemStack stack, OfflinePlayer owner) {
    SkullItem skullItem = new SkullItem(stack);
    skullItem.setOwner(owner);
    return skullItem;
  }

  public static SkullItem deserialize(Map<String, Object> args) {
    WrappedItemStack deserialized = WrappedItemStack.deserialize(args);
    SkullItem skullItem = new SkullItem(deserialized.getStack());
    if (StringUtils.isNotEmpty((String) args.get("owner")))
      skullItem.setOwner(Bukkit.getOfflinePlayer(UUID.fromString((String) args.get("owner"))));
    return skullItem;
  }

  @Override
  public @NonNull Map<String, Object> serialize() {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    OfflinePlayer owner = getOwner();
    if (owner != null)
      map.put("owner", owner.getUniqueId().toString());
    map.putAll(super.serialize());
    return map;
  }

  public void setOwner(@Nullable OfflinePlayer owner) {
    ItemStack stack = getStack();
    SkullMeta meta = (SkullMeta) stack.getItemMeta();
    Preconditions.checkNotNull(meta, "Stack has no meta");
    meta.setOwningPlayer(owner);
    stack.setItemMeta(meta);
  }

  public @Nullable OfflinePlayer getOwner() {
    ItemStack stack = getStack();
    SkullMeta meta = (SkullMeta) stack.getItemMeta();
    Preconditions.checkNotNull(meta, "Stack has no meta");
    return meta.getOwningPlayer();
  }

}
