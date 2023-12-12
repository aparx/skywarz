package io.github.aparx.skywarz.startup;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.SkywarsCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:24
 * @since 1.0
 */
public final class Main extends JavaPlugin {

  public static final String ROOT_COMMAND_NAME = "skywars";

  @Override
  public void onEnable() {
    Skywars.getInstance().load(this);

    SkywarsCommand command = new SkywarsCommand();
    PluginCommand skywars = getCommand(ROOT_COMMAND_NAME);
    Preconditions.checkNotNull(skywars);
    skywars.setExecutor(command);
    skywars.setTabCompleter(command);
  }


  @Override
  public void onDisable() {
    Skywars.getInstance().unload();
  }

}
