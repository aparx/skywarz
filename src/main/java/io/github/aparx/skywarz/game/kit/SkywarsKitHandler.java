package io.github.aparx.skywarz.game.kit;

import io.github.aparx.bufig.handler.ConfigProxy;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.handler.DefaultSkywarsHandler;
import io.github.aparx.skywarz.handler.SkywarsHandler;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import io.github.aparx.skywarz.utils.collection.KeyValueSets;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 20:57
 * @since 1.0
 */
@Getter
public final class SkywarsKitHandler extends DefaultSkywarsHandler {

  @Getter
  private static final SkywarsKitHandler instance = new SkywarsKitHandler();

  @Getter
  private static final ConfigProxy kitConfigProxy = new ConfigProxy((proxy) -> {
    return Skywars.getInstance().getConfigHandler().getOrCreate("kits");
  });

  private static final Collection<SkywarsKit> DEFAULT_KITS =
      Arrays.stream(DefaultKits.values())
          .map(DefaultKits::getKit)
          .collect(Collectors.toSet());

  private final KeyValueSet<String, SkywarsKit> kits =
      KeyValueSets.of((kit) -> kit.getName().toLowerCase());

  private SkywarsKitHandler() {}

  @Override
  protected void onLoad() {
    kitConfigProxy.load();
    for (Object value : kitConfigProxy.getValues(false).values())
      if (value instanceof SkywarsKit)
        kits.add((SkywarsKit) value);
    if (kits.isEmpty())
      kits.addAll(DEFAULT_KITS);
    save();
  }

  public void save() {
    kits.forEach((kit) -> {
      kitConfigProxy.set(kit.getName(), kit);
      kitConfigProxy.setDocsIfAbsent(kit.getName(),
          "Kit: " + kit.getName(),
          "Enchantments are done after the Bukkit enumeration. For all enchantments see:",
          "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html");
    });
    kitConfigProxy.save();
  }

  public @NonNull KeyValueSet<String, SkywarsKit> createSnapshot() {
    KeyValueSet<String, SkywarsKit> newSet = KeyValueSets.of(kits::getKey);
    kits.forEach((kit) -> newSet.add(kit.copy()));
    return newSet;
  }

}
