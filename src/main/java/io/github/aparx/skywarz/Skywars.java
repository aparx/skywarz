package io.github.aparx.skywarz;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.game.SpawnMap;
import io.github.aparx.skywarz.game.arena.*;
import io.github.aparx.skywarz.game.chest.ChestConfig;
import io.github.aparx.skywarz.game.chest.ChestItem;
import io.github.aparx.skywarz.game.item.GameItemManager;
import io.github.aparx.skywarz.game.kit.Kit;
import io.github.aparx.skywarz.game.kit.KitHandler;
import io.github.aparx.skywarz.game.scoreboard.MatchScoreboard;
import io.github.aparx.skywarz.handler.MainConfig;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import io.github.aparx.skywarz.handler.SkywarsHandler;
import io.github.aparx.skywarz.game.match.MatchManager;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.utils.collection.KeyedByClassSet;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import io.github.aparx.skywarz.utils.item.SkullItem;
import lombok.Getter;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:18
 * @since 1.0
 */
@Getter
public final class Skywars {

  static {
    ConfigurationSerialization.registerClass(Arena.class);
    ConfigurationSerialization.registerClass(ArenaBox.class);
    ConfigurationSerialization.registerClass(SpawnMap.class);
    ConfigurationSerialization.registerClass(GameSettings.class);
    ConfigurationSerialization.registerClass(ArenaData.class);
    ConfigurationSerialization.registerClass(WrappedItemStack.class);
    ConfigurationSerialization.registerClass(ChestItem.class);
    ConfigurationSerialization.registerClass(SkullItem.class);
    ConfigurationSerialization.registerClass(Kit.class);
  }

  @Getter
  private static final Skywars instance = new Skywars();

  private final KeyedByClassSet<SkywarsHandler> handlers = new HandlerSet();

  @Getter(onMethod_ = {@Synchronized})
  private volatile boolean isLoaded;

  private @NonNull Logger logger = Bukkit.getLogger();

  private Plugin plugin;

  private SkywarsConfigHandler configHandler;

  public static Plugin plugin() {
    return Preconditions.checkNotNull(instance.plugin, "Skywarz is not initialized");
  }

  public static @NonNull Logger logger() {
    return instance.logger;
  }

  private Skywars() {
    handlers.addAll(Set.of(
        new ArenaManager(),
        new MatchManager(),
        new GameItemManager()
    ));
  }

  @Synchronized
  public void load(Plugin plugin) {
    Preconditions.checkState(!isLoaded(), "Skywarz is already loaded");
    try {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
      logger.info("Loading plugin");
      configHandler = new SkywarsConfigHandler(plugin);
      MainConfig.getInstance().load();
      ChestConfig.getInstance().load();
      KitHandler.getInstance().load();
      Language.getInstance().load();
      for (MatchScoreboard board : MatchScoreboard.values())
        board.getScoreboard().load();
      getHandlers().forEach(SkywarsHandler::load);
      this.isLoaded = true;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Severe error on load", e);
      disablePlugin();
    }
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean unload() {
    if (!isLoaded()) return false;
    try {
      logger.info("Unloading plugin");
      getHandlers().forEach(SkywarsHandler::unload);
      return true;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not finalize plugin unload", e);
      disablePlugin();
      return false;
    } finally {
      isLoaded = false;
      plugin = null;
      logger = Bukkit.getLogger();
    }
  }

  @CanIgnoreReturnValue
  public boolean disablePlugin() {
    if (!plugin.isEnabled()) return false;
    Bukkit.getPluginManager().disablePlugin(plugin);
    return true;
  }

  public @NonNull ArenaManager getArenaManager() {
    return getHandlers().require(ArenaManager.class);
  }

  public @NonNull MatchManager getMatchManager() {
    return getHandlers().require(MatchManager.class);
  }

  public @NonNull GameItemManager getGameItemManager() {
    return getHandlers().require(GameItemManager.class);
  }

  private final class HandlerSet extends KeyedByClassSet<SkywarsHandler> {

    @Override
    public boolean add(SkywarsHandler skywarsHandler) {
      if (!super.add(skywarsHandler)) return false;
      if (isLoaded()) skywarsHandler.load();
      logger.log(Level.FINE, "Added handler {0}", skywarsHandler);
      return true;
    }

    @Override
    public boolean remove(Object value) {
      if (!super.remove(value)) return false;
      ((SkywarsHandler) value).unload();
      logger.log(Level.FINE, "Removed handler {0}", value);
      return true;
    }
  }

}
