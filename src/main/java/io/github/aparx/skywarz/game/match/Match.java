package io.github.aparx.skywarz.game.match;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.WeakGroupAudience;
import io.github.aparx.skywarz.entity.data.types.MainPlayerData;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.game.arena.snapshot.ArenaSnapshot;
import io.github.aparx.skywarz.handler.configs.Language;
import io.github.aparx.skywarz.utils.Snowflake;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 05:20
 * @since 1.0
 */
@Getter
public class Match implements Snowflake<UUID> {

  private final @NonNull UUID id;

  /** Returns a snapshot of an arena, so it is still valid for this match on allocation. */
  private final @NonNull ArenaSnapshot arena;

  @Getter(onMethod_ = {@Synchronized})
  @Setter(onMethod_ = {@Synchronized})
  private volatile @NonNull MatchState state = MatchState.LOBBY;

  private final WeakGroupAudience<SkywarsPlayer> audience = new WeakGroupAudience<>();

  public Match(@NonNull UUID id, @NonNull Arena arena) {
    Preconditions.checkNotNull(id, "ID must not be null");
    Preconditions.checkNotNull(arena, "Arena must not be null");
    Preconditions.checkState(arena.isCompleted(), "Arena must be completed");
    this.id = id;
    this.arena = new ArenaSnapshot(arena);
  }

  @Synchronized
  public boolean isState(MatchState state) {
    return this.state == state;
  }

  @CanIgnoreReturnValue
  public boolean join(@NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    Preconditions.checkState(getState().isJoinable());
    MainPlayerData data = player.getPlayerData().getOrCreate(MainPlayerData.class);
    Preconditions.checkState(!data.isInMatch(), "Already in a match");
    if (!getAudience().add(player)) return false;
    data.setMatch(this);
    // TODO call event
    getAudience().sendMessage((lang) -> lang.substitute(lang.getBroadcastJoinedMatch(),
        Language.newValueMapFromPlayer(player.getOnline(), "player")));
    return true;
  }

  @CanIgnoreReturnValue
  public boolean leave(@NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    if (!getAudience().remove(player)) return false;
    MainPlayerData data = player.getPlayerData().getOrCreate(MainPlayerData.class);
    data.setMatch(null);
    // TODO call event
    getAudience().sendMessage((lang) -> lang.substitute(lang.getBroadcastLeftMatch(),
        Language.newValueMapFromPlayer(player.getOnline(), "player")));
    return true;
  }

}
