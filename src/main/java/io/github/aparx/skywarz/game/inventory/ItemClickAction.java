package io.github.aparx.skywarz.game.inventory;

import io.github.aparx.skywarz.entity.GamePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 13:06
 * @since 1.0
 */
@FunctionalInterface
public interface ItemClickAction {

  void click(GamePlayer player, InventoryClickEvent event);

}
