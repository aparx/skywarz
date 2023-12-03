package io.github.aparx.skywarz;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.skywars.arena.Arena;
import io.github.aparx.skywarz.skywars.arena.ArenaManager;
import io.github.aparx.skywarz.skywars.arena.MutableArenaBox;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import io.github.aparx.skywarz.handler.SkywarsHandler;
import io.github.aparx.skywarz.skywars.arena.SpawnList;
import io.github.aparx.skywarz.utils.collection.KeyedByClassSet;
import lombok.Getter;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
    Set.of(
        Arena.class,
        MutableArenaBox.class,
        SpawnList.class
    ).forEach(ConfigurationSerialization::registerClass);
  }

  @Getter
  private static final Skywars instance = new Skywars();

  private final KeyedByClassSet<SkywarsHandler> handlers = new HandlerSet();

  @Getter(onMethod_ = {@Synchronized})
  private volatile boolean isLoaded;

  private @NonNull Logger logger = Bukkit.getLogger();

  private Plugin plugin;

  private SkywarsConfigHandler configHandler;

  public static @Nullable Plugin plugin() {
    return instance.plugin;
  }

  public static @NonNull Logger logger() {
    return instance.logger;
  }

  private Skywars() {
    handlers.addAll(Set.of(
        new ArenaManager()
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
      configHandler.getMain().load(); // TODO handle in handler?
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
    if (isLoaded()) return false;
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
