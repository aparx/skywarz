package io.github.aparx.skywarz.startup;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Magics;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.SkywarsCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:24
 * @since 1.0
 */
public final class Main extends JavaPlugin {

  public static final String FULL_COMMAND = "skywars";

  public static final String SHORT_COMMAND = "sw";

  @Override
  public void onEnable() {
    Magics.setDevelopment(getDescription().getVersion().contains("dev"));
    Skywars.getInstance().load(this);

    SkywarsCommand command = new SkywarsCommand();
    PluginCommand skywars = getCommand(FULL_COMMAND);
    Preconditions.checkNotNull(skywars);
    skywars.setExecutor(command);
    skywars.setTabCompleter(command);
  }


  @Override
  public void onDisable() {
    Skywars.getInstance().unload();
  }

}
