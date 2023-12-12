package io.github.aparx.skywarz.command.commands.kit;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.SkywarsPlayerData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.kit.GameKit;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.ref.WeakReference;
import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 18:07
 * @since 1.0
 */
@Getter
@Setter
public class PlayerKitEditMode extends SkywarsPlayerData {

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
        player.playActionbar("§7Modifying kit: §e" + getKit().getName());
      } catch (Exception e) {
        leave();
      }
    }, 0, TickDuration.ofSecond().toTicks());
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

}
