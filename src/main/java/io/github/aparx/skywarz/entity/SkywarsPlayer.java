package io.github.aparx.skywarz.entity;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.RegisterNotifiable;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.database.object.CachableLazyObject;
import io.github.aparx.skywarz.entity.data.SkywarsPlayerDataSet;
import io.github.aparx.skywarz.entity.data.SkywarsPlayerData;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsAccumulator;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import io.github.aparx.skywarz.utils.Snowflake;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import io.github.aparx.skywarz.utils.collection.KeyValueSets;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 04:36
 * @since 1.0
 */
@Getter
public final class SkywarsPlayer implements Snowflake<UUID>, Audience, RegisterNotifiable {

  private static final Supplier<? extends RuntimeException> ERROR_PLAYER_INVALID =
      () -> new IllegalStateException("Player has become invalid (left?)");

  private static final KeyValueSet<UUID, SkywarsPlayer> playerMap = KeyValueSets.ofNotifable();

  private static Listener quitListener = null;

  private final @NonNull UUID id;

  private final SkywarsPlayerDataSet<SkywarsPlayerData> playerData =
      new SkywarsPlayerDataSet<>(this);

  private SkywarsPlayer(@NonNull UUID id) {
    Preconditions.checkNotNull(id, "ID must not be null");
    this.id = id;
  }

  public static void removeAllPlayers() {
    playerMap.forEach(SkywarsPlayer::notifyRemoval);
    playerMap.clear();
  }

  public static @NonNull SkywarsPlayer getPlayer(@NonNull UUID uniqueId) {
    Preconditions.checkNotNull(uniqueId, "ID must not be null");
    loadListenerIfNeeded();
    return playerMap.computeIfAbsent(uniqueId, SkywarsPlayer::new);
  }

  public static @NonNull SkywarsPlayer getPlayer(@NonNull Player player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    return getPlayer(player.getUniqueId());
  }

  public static Optional<SkywarsPlayer> findPlayer(@NonNull UUID uniqueId) {
    Preconditions.checkNotNull(uniqueId, "ID must not be null");
    loadListenerIfNeeded();
    return playerMap.find(uniqueId);
  }

  public static Optional<SkywarsPlayer> findPlayer(@NonNull Player player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    return findPlayer(player.getUniqueId());
  }

  public static void forPlayers(Consumer<SkywarsPlayer> action) {
    playerMap.forEach(action);
  }

  @CanIgnoreReturnValue
  public static boolean removePlayer(@NonNull UUID uniqueId) {
    return playerMap.remove(playerMap.get(uniqueId));
  }

  @CanIgnoreReturnValue
  public static boolean removePlayer(@NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    return playerMap.remove(player);
  }

  private static void loadListenerIfNeeded() {
    if (quitListener != null) return;
    synchronized (SkywarsPlayer.class) {
      if (quitListener != null) return;
      Bukkit.getPluginManager().registerEvents(quitListener = new Listener() {
        /* MONITOR so that the removal of the player doesn't interfere with external dependencies */
        @EventHandler(priority = EventPriority.MONITOR)
        void onQuit(PlayerQuitEvent event) {
          removePlayer(event.getPlayer().getUniqueId());
        }
      }, Skywars.plugin());
    }
  }

  @Override
  public void notifyRegister() {}

  @Override
  public void notifyRemoval() {
    getPlayerData().clear();
  }

  public Optional<Player> findOnline() {
    return Optional.ofNullable(Bukkit.getPlayer(getId()));
  }

  public @NonNull Player getOnline() {
    return findOnline().orElseThrow(ERROR_PLAYER_INVALID);
  }

  public @NonNull OfflinePlayer getOffline() {
    return Bukkit.getOfflinePlayer(getId());
  }

  public boolean isOnline() {
    return findOnline().isPresent();
  }

  public @NonNull String getName() {
    return Optional.ofNullable(getOffline().getName()).orElse(id.toString());
  }

  public @NonNull String getDisplayName() {
    return findOnline().map(Player::getDisplayName).orElseGet(this::getName);
  }

  public PlayerSnapshot takeSnapshot() {
    return PlayerSnapshot.of(getOnline());
  }

  public @NonNull PlayerMatchData getMatchData() {
    return getPlayerData().getOrCreate(PlayerMatchData.class);
  }

  public boolean hasPriority() {
    return findOnline().filter(SkywarsPermission.PRIORITY::has).isPresent();
  }

  public CachableLazyObject<PlayerStatsAccumulator> getTotalStats() {
    return Skywars.getInstance().getDatabase().getStatsManager()
        .getRegistry().getOrCreate(getId());
  }

  public void sendMessage(Object message) {
    findOnline().ifPresent((p) -> p.sendMessage(String.valueOf(message)));
  }

  @Override
  public void playSound(Sound sound, float volume, float pitch) {
    findOnline().ifPresent((p) -> p.playSound(p.getLocation(), sound, volume, pitch));
  }

  @Override
  public void playSound(Location location, Sound sound, float volume, float pitch) {
    findOnline().ifPresent((p) -> p.playSound(location, sound, volume, pitch));
  }

  @Override
  public void playTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    findOnline().ifPresent((p) -> p.sendTitle(title, subtitle, fadeIn, stay, fadeOut));
  }

  @Override
  public void playActionbar(String message) {
    findOnline().ifPresent((p) -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
        TextComponent.fromLegacyText(message)));
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    SkywarsPlayer player = (SkywarsPlayer) object;
    return Objects.equals(id, player.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
