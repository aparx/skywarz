package io.github.aparx.skywarz.entity.snapshot;

import com.google.common.eventbus.AllowConcurrentEvents;
import io.github.aparx.skywarz.game.match.Match;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A data object representing data of a player at a certain point in time. The data object may
 * not be directly mutable, but is passively due to Bukkit's API.
 *
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 04:28
 * @since 1.0
 */
@Getter
@Builder
@RequiredArgsConstructor(staticName = "of")
public final class PlayerSnapshot {

  private final ItemStack[] items;
  private final Collection<PotionEffect> potionEffects;
  private final Location location;
  private final double maxHealth;
  private final double health;
  private final int foodLevel;
  private final String displayName;
  private final String playerListName;
  private final GameMode gameMode;
  private final boolean isAllowedFlying;
  private final boolean isFlying;
  private final Float flySpeed;
  private final float exp;
  private final int level;

  public static PlayerSnapshot ofReset(@NonNull Player player) {
    return ofReset(player, null, GameMode.SURVIVAL);
  }

  public static PlayerSnapshot ofReset(@NonNull Player player, GameMode mode) {
    return ofReset(player, null, mode);
  }

  public static PlayerSnapshot ofReset(@NonNull Player player, Location location, GameMode mode) {
    return new PlayerSnapshot(new ItemStack[player.getInventory().getContents().length],
        List.of(), location, 20, 20, 20, player.getName(), null, mode, false, false, 0.1F, 0.0F, 0);
  }

  public static PlayerSnapshot ofSpectator(@NonNull Match match, @NonNull Player player) {
    return new PlayerSnapshot(new ItemStack[player.getInventory().getContents().length],
        List.of(), match.getArena().getData().getSpectator(), 2, 2, 20, player.getDisplayName(),
        player.getPlayerListName(), GameMode.ADVENTURE, true, true, 0.1F, 0F, 0);
  }

  public static PlayerSnapshot of(@NonNull Player player) {
    return new PlayerSnapshot(
        (ItemStack[]) ArrayUtils.clone(player.getInventory().getContents()),
        new ArrayList<>(player.getActivePotionEffects()),
        player.getLocation().clone(),
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
        player.getHealth(),
        player.getFoodLevel(),
        player.getDisplayName(),
        player.getPlayerListName(),
        player.getGameMode(),
        player.getAllowFlight(),
        player.isFlying(),
        player.getFlySpeed(),
        player.getExp(),
        player.getLevel()
    );
  }

  public void restore(@NonNull Player player) {
    if (items != null)
      player.getInventory().setContents(items);
    if (potionEffects != null)
      player.addPotionEffects(potionEffects);
    if (location != null)
      player.teleport(location);
    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    player.setHealth(health);
    player.setFoodLevel(foodLevel);
    player.setDisplayName(displayName);
    if (playerListName != null)
      player.setPlayerListName(playerListName);
    if (gameMode != null)
      player.setGameMode(gameMode);
    player.setAllowFlight(isAllowedFlying);
    player.setFlying(isFlying);
    if (flySpeed != null)
      player.setFlySpeed(flySpeed);
    player.setExp(exp);
    player.setLevel(level);
  }

}
