package io.github.aparx.skywarz.game.phase.features;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.item.items.LeaveItem;
import io.github.aparx.skywarz.game.item.items.playing.TeleporterItem;
import io.github.aparx.skywarz.game.match.GameMatch;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 08:16
 * @since 1.0
 */
@UtilityClass
public final class GameSpectator {

  public static void markAsSpectator(Player entity) {
    SkywarsPlayer player = SkywarsPlayer.getPlayer(entity);
    player.getMatchData().setSpectator(true);
    String displayName = String.valueOf(ChatColor.GRAY) + ChatColor.ITALIC + entity.getName();
    entity.setDisplayName(displayName);
    entity.setPlayerListName(displayName);
  }

  /** Modifies and teleports {@code entity} to be a spectator. */
  public static void spawnAsSpectator(GameMatch match, Player entity) {
    markAsSpectator(entity); // ensure the marking
    PlayerSnapshot.ofSpectator(match, entity).restore(entity);
    match.getAudience().alive()
        .map(SkywarsPlayer::getOnline)
        .forEach((other) -> other.hidePlayer(Skywars.plugin(), entity));
    Skywars.getInstance()
        .getItemManager()
        .getItems()
        .require(TeleporterItem.class)
        .give(match, entity);
    entity.getInventory().setItem(LeaveItem.SLOT, Skywars.getInstance()
        .getItemManager()
        .getItems()
        .require(LeaveItem.class)
        .create(match, entity));
  }

  public static void removeSpectator(GameMatch match, Player entity) {
    SkywarsPlayer.getPlayer(entity).getMatchData().setSpectator(false);
    match.getAudience().entity().forEach((other) -> {
      other.showPlayer(Skywars.plugin(), entity);
      entity.showPlayer(Skywars.plugin(), other);
    });
  }

}
