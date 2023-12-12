package io.github.aparx.skywarz.game.inventory;

import io.github.aparx.skywarz.entity.SkywarsPlayer;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 13:06
 * @since 1.0
 */
@FunctionalInterface
public interface ItemClickAction {

  void click(SkywarsPlayer player, InventoryClickEvent event);

}
