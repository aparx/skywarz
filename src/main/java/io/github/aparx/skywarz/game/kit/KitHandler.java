package io.github.aparx.skywarz.game.kit;

import io.github.aparx.bufig.handler.ConfigProxy;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import io.github.aparx.skywarz.utils.collection.KeyValueSets;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 20:57
 * @since 1.0
 */
@Getter
public final class KitHandler {

  @Getter
  private static final KitHandler instance = new KitHandler();

  private static final Collection<Kit> DEFAULT_KITS =
      Arrays.stream(DefaultKits.values())
          .map(DefaultKits::getKit)
          .collect(Collectors.toSet());

  private final KeyValueSet<String, Kit> kits =
      KeyValueSets.of((kit) -> kit.getName().toLowerCase());

  private final ConfigProxy proxy = new ConfigProxy((proxy) -> {
    return Skywars.getInstance().getConfigHandler().getOrCreate("kits");
  });

  private KitHandler() {}

  public void load() {
    proxy.load();
    for (Object value : proxy.getValues(false).values())
      if (value instanceof Kit)
        kits.add((Kit) value);
    if (kits.isEmpty())
      kits.addAll(DEFAULT_KITS);
    save();
  }

  public void save() {
    kits.forEach((kit) -> {
      proxy.set(kit.getName(), kit);
      proxy.setDocsIfAbsent(kit.getName(),
          "Kit: " + kit.getName(),
          "Enchantments are done after the Bukkit enumeration. For all enchantments see:",
          "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html");
    });
    proxy.save();
  }

  public @NonNull KeyValueSet<String, Kit> createSnapshot() {
    KeyValueSet<String, Kit> newSet = KeyValueSets.of(kits::getKey);
    kits.forEach((kit) -> newSet.add(kit.copy()));
    return newSet;
  }

}
