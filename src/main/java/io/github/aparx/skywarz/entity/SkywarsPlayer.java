package io.github.aparx.skywarz.entity;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.entity.data.PlayerDataSet;
import io.github.aparx.skywarz.entity.data.SkywarsPlayerData;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.permission.Permission;
import io.github.aparx.skywarz.utils.Snowflake;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 04:36
 * @since 1.0
 */
@Getter
public final class SkywarsPlayer implements Snowflake<UUID>, Audience {

  private static final Supplier<? extends RuntimeException> ERROR_PLAYER_INVALID =
      () -> new IllegalStateException("Player has become invalid (left?)");

  private static final Map<UUID, SkywarsPlayer> playerMap = new ConcurrentHashMap<>();

  private final @NonNull UUID id;

  private final PlayerDataSet<SkywarsPlayerData> playerData = new PlayerDataSet<>(this);

  private SkywarsPlayer(@NonNull UUID id) {
    Preconditions.checkNotNull(id, "ID must not be null");
    this.id = id;
  }

  public static @NonNull SkywarsPlayer getPlayer(@NonNull UUID uniqueId) {
    Preconditions.checkNotNull(uniqueId, "ID must not be null");
    return playerMap.computeIfAbsent(uniqueId, SkywarsPlayer::new);
  }

  public static @NonNull SkywarsPlayer getPlayer(@NonNull Player player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    return getPlayer(player.getUniqueId());
  }

  public static Optional<SkywarsPlayer> findPlayer(@NonNull UUID uniqueId) {
    Preconditions.checkNotNull(uniqueId, "ID must not be null");
    return Optional.ofNullable(playerMap.get(uniqueId));
  }

  public static Optional<SkywarsPlayer> findPlayer(@NonNull Player player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    return findPlayer(player.getUniqueId());
  }

  @CanIgnoreReturnValue
  public static @NonNull SkywarsPlayer removePlayer(@NonNull UUID uniqueId) {
    return playerMap.remove(uniqueId); // TODO call player.onRemove()?
  }

  @CanIgnoreReturnValue
  public static boolean removePlayer(@NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    return playerMap.remove(player.getId(), player);
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

  public PlayerSnapshot createPlayerSnapshot() {
    return PlayerSnapshot.of(getOnline());
  }

  public @NonNull PlayerMatchData getMatchData() {
    return getPlayerData().getOrCreate(PlayerMatchData.class);
  }

  public boolean hasPriority() {
    return findOnline().filter(Permission.PRIORITY::has).isPresent();
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

}
