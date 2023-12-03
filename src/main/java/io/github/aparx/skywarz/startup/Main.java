package io.github.aparx.skywarz.startup;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.SkywarzCommand;
import io.github.aparx.skywarz.handler.configs.Language;
import io.github.aparx.skywarz.skywars.arena.Arena;
import io.github.aparx.skywarz.skywars.arena.MutableArenaBox;
import io.github.aparx.skywarz.skywars.arena.SpawnList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:24
 * @since 1.0
 */
public final class Main extends JavaPlugin {

  @Override
  public void onEnable() {
    Skywars.getInstance().load(this);

    SkywarzCommand command = new SkywarzCommand();
    PluginCommand skywars = getCommand("skywars");
    skywars.setExecutor(command);
    skywars.setTabCompleter(command);

    Arena arena = new Arena("testArena");
    arena.setWorld(Bukkit.getWorld("world"));
    arena.getBox().setPoint(MutableArenaBox.Point.MIN, new BlockVector(30, 70, 30));
    arena.getBox().setPoint(MutableArenaBox.Point.MAX, new BlockVector(20, 80, 60));
    arena.setSpectator(new Location(Bukkit.getWorld("world"), 30, 70, 40));

    Bukkit.getConsoleSender().sendMessage(Language.getLanguage().substitute(Language::getLoadedLog));
  }

  @Override
  public void onDisable() {
    Skywars.getInstance().unload();
    Bukkit.getConsoleSender().sendMessage(Language.getLanguage().substitute(Language::getUnloadedLog));
  }
}
