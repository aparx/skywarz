package io.github.aparx.skywarz.startup;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.SkywarsCommand;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.game.scoreboard.SpecialScoreboard;
import io.github.aparx.skywarz.game.scoreboard.MatchScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:24
 * @since 1.0
 */
public final class Main extends JavaPlugin implements Listener {

  public static final String COMMAND_NAME = "skywars";

  @Override
  public void onEnable() {
    Skywars.getInstance().load(this);

    SkywarsCommand command = new SkywarsCommand();
    PluginCommand skywars = getCommand(COMMAND_NAME);
    Preconditions.checkNotNull(skywars);
    skywars.setExecutor(command);
    skywars.setTabCompleter(command);

    Bukkit.getPluginManager().registerEvents(this, this);
  }

  @Override
  public void onDisable() {
    Skywars.getInstance().unload();
  }

  int cycle = 0;

  @EventHandler
  void interact(PlayerInteractEvent e) {
    //DefaultKits.values()[(cycle++) % DefaultKits.values().length].getKit().apply(e.getPlayer());
  }

}
