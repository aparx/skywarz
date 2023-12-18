package io.github.aparx.skywarz.game.kit;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.handler.ConfigProxy;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.handler.DefaultSkywarsHandler;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import io.github.aparx.skywarz.utils.collection.AbstractKeyValueSet;
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
public final class GameKitManager extends DefaultSkywarsHandler {

  @Getter
  private static final GameKitManager instance = new GameKitManager();

  @Getter
  private static final ConfigProxy kitConfigProxy = new ConfigProxy((proxy) -> {
    return Skywars.getInstance().getConfigHandler().getOrCreate("kits");
  });

  private static final Collection<GameKit> DEFAULT_KITS =
      Arrays.stream(DefaultKits.values())
          .map(DefaultKits::getKit)
          .collect(Collectors.toSet());

  private final KeyValueSet<String, GameKit> kits = new AbstractKeyValueSet<>() {
    @Override
    public String getKey(GameKit kit) {
      return transformKey(kit.getName());
    }

    @Override
    public boolean remove(Object value) {
      if (!super.remove(value)) return false;
      kitConfigProxy.set(((GameKit) value).getName(), null);
      return true;
    }
  };

  private GameKitManager() {}

  private static String transformKey(String key) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return key.toLowerCase();
  }

  @Override
  protected void onLoad() {
    kitConfigProxy.load();
    kitConfigProxy.setHeaderIfAbsent(SkywarsConfigHandler.createHeader(
        "Kits configuration",
        "Edit this config manually to create, delete and manage kits!",
        "Note: every kit has an icon. Currently, it is only possible to edit the icon of a kit",
        "via this configuration, but you can create kits and their content with '/sw kit' in-game!"
    ));
    for (Object value : kitConfigProxy.getValues(false).values())
      if (value instanceof GameKit)
        kits.add((GameKit) value);
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

  public @NonNull KeyValueSet<String, GameKit> createSnapshot() {
    KeyValueSet<String, GameKit> newSet = KeyValueSets.of(kits::getKey);
    kits.forEach((kit) -> newSet.add(kit.copy()));
    return newSet;
  }

  public Optional<GameKit> find(@NonNull String name) {
    return kits.find(transformKey(name));
  }

  public GameKit get(@NonNull String name) {
    return kits.require(transformKey(name));
  }

  public boolean contains(@NonNull String name) {
    return kits.containsKey(transformKey(name));
  }

}
