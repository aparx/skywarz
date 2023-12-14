package io.github.aparx.skywarz.bungeecord;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.handler.DefaultSkywarsHandler;
import io.github.aparx.skywarz.handler.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-13 16:58
 * @since 1.0
 */
public class BungeeHandler extends DefaultSkywarsHandler {

  private static final String CHANNEL = "BungeeCord";

  private final BungeeListener listener = new BungeeListener();

  @Override
  protected void onLoad() {
    Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(Skywars.plugin(), CHANNEL);
    Bukkit.getPluginManager().registerEvents(listener, Skywars.plugin());
  }

  @Override
  protected void onUnload() {
    Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(Skywars.plugin());
    HandlerList.unregisterAll(listener);
  }

  public void sendToFallback(Player player) {
    sendToServer(player, MainConfig.getInstance().getBungeeFallback());
  }

  public void sendToServer(Player player, String serverName) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("Connect");
    out.writeUTF(serverName);
    player.sendPluginMessage(Skywars.plugin(), CHANNEL, out.toByteArray());
  }

}
