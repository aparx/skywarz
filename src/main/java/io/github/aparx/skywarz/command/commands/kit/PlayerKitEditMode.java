package io.github.aparx.skywarz.command.commands.kit;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.SkywarsPlayerData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.kit.GameKit;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 18:07
 * @since 1.0
 */
@Getter
@Setter
public class PlayerKitEditMode extends SkywarsPlayerData implements Listener {

  private final @NonNull SkywarsPlayer player;

  private @NonNull WeakReference<GameKit> kit = new WeakReference<>(null);

  private PlayerSnapshot snapshot;

  @Setter(AccessLevel.NONE)
  private boolean isInMode = false;

  private BukkitTask updateTask;

  public PlayerKitEditMode(@NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    this.player = player;
  }

  @CanIgnoreReturnValue
  public boolean enter(GameKit kit) {
    if (isInMode) return false;
    final Player entity = player.getOnline();
    this.kit = new WeakReference<>(kit);
    isInMode = true;
    snapshot = player.takeSnapshot();
    PlayerSnapshot.ofReset(entity, GameMode.CREATIVE).restore(entity);
    kit.apply(entity); // apply current kit to player
    updateTask = Bukkit.getScheduler().runTaskTimer(Skywars.plugin(), () -> {
      try {
        player.playActionbar(ChatColor.GRAY + "Modifying kit: " + getKit().getDisplayName());
      } catch (Exception e) {
        leave();
      }
    }, 0, TickDuration.ofSecond().toTicks());
    Bukkit.getPluginManager().registerEvents(this, Skywars.plugin());
    return true;
  }

  @CanIgnoreReturnValue
  public boolean leave() {
    if (!isInMode) return false;
    isInMode = false;
    if (updateTask != null)
      updateTask.cancel();
    updateTask = null;
    player.findOnline().ifPresent((online) -> {
      if (snapshot != null)
        snapshot.restore(online);
    });
    HandlerList.unregisterAll(this);
    return true;
  }

  public Optional<GameKit> findKit() {
    return Optional.ofNullable(kit.get());
  }

  public @NonNull GameKit getKit() {
    GameKit gameKit = kit.get();
    Preconditions.checkState(gameKit != null, "Kit has become invalid (deleted)");
    return gameKit;
  }

  @Override
  public void notifyRemoval() {
    leave();
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onTeleport(PlayerTeleportEvent event) {
    Player entity = event.getPlayer();
    Location to = event.getTo();
    Location from = event.getFrom();
    if (to != null && !to.equals(from) && !event.isCancelled())
      SkywarsPlayer.findPlayer(entity)
          .filter((p) -> p.equals(player))
          .map(SkywarsPlayer::getPlayerData)
          .flatMap((data) -> data.find(PlayerKitEditMode.class))
          .filter(PlayerKitEditMode::isInMode)
          .ifPresent((editMode) -> {
            editMode.leave();
            entity.sendMessage(Language.getInstance().substitute(
                "{successPrefix} Left kit edit mode due to teleport."));
          });
  }

}
