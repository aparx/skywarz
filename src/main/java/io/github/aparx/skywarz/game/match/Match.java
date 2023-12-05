package io.github.aparx.skywarz.game.match;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.WeakPlayerGroup;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.game.arena.snapshot.ArenaSnapshot;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.game.team.TeamMap;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.Snowflake;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
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
  private volatile @NonNull ArenaSnapshot arena;

  @Getter(onMethod_ = {@Synchronized})
  @Setter(onMethod_ = {@Synchronized})
  private volatile @NonNull MatchState state = MatchState.SETUP;

  private final GamePhaseCycler cycler = new GamePhaseCycler(this);

  private final MatchWatchTask watchTask = new MatchWatchTask(this);

  private final TeamMap teamMap = new TeamMap(this);

  private final WeakPlayerGroup audience = new WeakPlayerGroup() {
    @Override
    public boolean add(SkywarsPlayer player) {
      if (!super.add(player)) return false;
      player.getMatchData().setMatch(Match.this);
      return true;
    }

    @Override
    public boolean remove(Object o) {
      if (!super.remove(o)) return false;
      ((SkywarsPlayer) o).getMatchData().setMatch(null);
      return true;
    }
  };

  public Match(@NonNull UUID id, @NonNull Arena arena) {
    Preconditions.checkNotNull(id, "ID must not be null");
    Preconditions.checkNotNull(arena, "Arena must not be null");
    this.id = id;
    this.arena = new ArenaSnapshot(arena);
  }

  /** Called by the {@code MatchManager} when this match has been registered. */
  @Synchronized
  public void notifyRegister() {
    // Starts the lobby
    Preconditions.checkState(arena.isCompleted(), "Arena must be completed");
    Arena arenaSource = arena.getSource();
    Preconditions.checkNotNull(arenaSource, "Arena has become invalid");
    this.arena = new ArenaSnapshot(arenaSource);
    this.teamMap.createTeams();
    cycler.cycleJump(MatchState.WAITING);

    getWatchTask().start();
  }

  /** Called by the {@code MatchManager} when this match has been removed. */
  public void notifyRemoval() {
    // Stops the lobby
    getWatchTask().stop();
    getAudience().forEach(this::leave);
  }

  @Synchronized
  public boolean isState(MatchState state) {
    return this.state == state;
  }

  @CanIgnoreReturnValue
  public boolean join(@NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    PlayerMatchData data = player.getMatchData();
    if (data.isInMatch() || !getState().isJoinable())
      return false;
    if (!getAudience().add(player)) return false;
    data.setSnapshot(player.createPlayerSnapshot());
    getAudience().sendMessage((language) -> language
        .get(MessageKeys.Match.JOIN_BROADCAST)
        .substitute(player.getOnline(), ArrayPath.of("player")));
    getCycler().getPhase().ifPresent((phase) -> phase.join(player));
    return true;
  }

  @CanIgnoreReturnValue
  public boolean leave(@NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    if (!getAudience().remove(player)) return false;
    PlayerMatchData data = player.getMatchData();
    Optional.ofNullable(data.getTeam()).ifPresent((team) -> team.remove(player));
    getAudience().sendMessage((language) -> language
        .get(MessageKeys.Match.LEAVE_BROADCAST)
        .substitute(player.getOnline(), ArrayPath.of("player")));

    // Manage entity
    player.findOnline().ifPresent((entity) -> {
      PlayerSnapshot snapshot = data.getSnapshot();
      Preconditions.checkNotNull(snapshot, "Cannot restore entity (snapshot removed)");
      snapshot.restore(entity);
    });
    return true;
  }

}
