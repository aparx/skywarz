package io.github.aparx.skywarz.startup;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.SkywarsCommand;
import io.github.aparx.skywarz.game.inventory.GameInventory;
import io.github.aparx.skywarz.game.inventory.InventoryDimensions;
import io.github.aparx.skywarz.game.inventory.InventoryItem;
import io.github.aparx.skywarz.game.inventory.content.InventoryPage;
import io.github.aparx.skywarz.game.inventory.content.PaginatableInventoryContent;
import io.github.aparx.skywarz.game.inventory.content.PaginatingInventory;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.SkullItem;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;

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
    skywars.setExecutor(command);
    skywars.setTabCompleter(command);

    //Bukkit.getPluginManager().registerEvents(this, this);
  }

  @Override
  public void onDisable() {
    Skywars.getInstance().unload();
  }

  @EventHandler
  void interact(PlayerInteractEvent e) {
    ArrayList<InventoryItem> items = new ArrayList<>();
    PaginatingInventory inventory = new PaginatingInventory(null, TickDuration.ofTick(),
        InventoryDimensions.ofRows(2), items, "Test pagination");
    fillItems(items);
    inventory.open(e.getPlayer());
  }

  private final ItemStack stack = SkullItem.of(ItemBuilder.builder()
          .material(Material.PLAYER_HEAD)
          .build(),
      Bukkit.getOfflinePlayer("Chesthead"))
      .getStack();

  void fillItems(ArrayList<InventoryItem> list) {
    for (int i = 0; i <= 16; ++i)
      list.add(InventoryItem.of(stack));
  }

}
