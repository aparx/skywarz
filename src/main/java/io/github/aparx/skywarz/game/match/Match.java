package io.github.aparx.skywarz.game.match;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Magics;
import io.github.aparx.skywarz.game.arena.reset.ArenaReset;
import io.github.aparx.skywarz.game.chest.ChestConfig;
import io.github.aparx.skywarz.game.chest.ChestHandler;
import io.github.aparx.skywarz.language.LocalizableError;
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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

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

  private final ChestHandler chestHandler;

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
    this.chestHandler = new ChestHandler(arena, ChestConfig.getInstance().getItems());
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
    getChestHandler().reset();
    // enforce map reset
    Optional.ofNullable(getArena().getSource())
        .map(Arena::getReset)
        .ifPresent(ArenaReset::reset);
  }

  @Synchronized
  public boolean isState(MatchState state) {
    return this.state == state;
  }

  public int getTeamSize() {
    return getArena().getData().getSettings().getTeamSize();
  }

  public int getMaxPlayerSize() {
    return getTeamMap().size() * getTeamSize();
  }

  public int getMinPlayerSize() {
    return (Magics.isDevelopment() ? 0 : 1) + getTeamSize();
  }

  @CanIgnoreReturnValue
  public boolean join(@NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    PlayerMatchData data = player.getMatchData();
    if (audience.contains(player)
        || data.isInMatch()
        || !getState().isJoinable())
      return false;
    boolean isExceedingPlayerSize = isState(MatchState.WAITING)
        && getAudience().size() >= getMaxPlayerSize();
    if (isExceedingPlayerSize && !player.hasPriority())
      throw new LocalizableError("Max players exceeded",
          (lang) -> lang.get(MessageKeys.Errors.MATCH_IS_FULL));
    if (isExceedingPlayerSize) kickFirstNonPriorityPlayer();
    if (!audience.add(player)) return false;
    data.setSnapshot(player.createPlayerSnapshot());
    try {
      getCycler().getPhase().ifPresent((phase) -> phase.handleJoin(player));
      if (!data.isSpectator())
        audience.sendMessage(Language.getInstance()
            .get(MessageKeys.Match.JOIN_BROADCAST)
            .substitute(player.getOnline(), ArrayPath.of("player")));
      return true;
    } catch (Exception e) {
      leave(player);
      player.sendMessage(Language.getInstance().substitute(MessageKeys.Match.JOIN_ERROR));
      return false;
    }
  }

  @CanIgnoreReturnValue
  public boolean leave(@NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    if (!getAudience().remove(player)) return false;
    PlayerMatchData data = player.getMatchData();
    // (1) remove from team
    Optional.ofNullable(data.getTeam())
        .ifPresent((team) -> team.remove(player));
    // (2) announce of leave
    if (!data.isSpectator())
      getAudience().sendMessage(Language.getInstance()
          .get(MessageKeys.Match.LEAVE_BROADCAST)
          .substitute(player.getOnline(), ArrayPath.of("player")));
    getCycler().getPhase().ifPresent((phase) -> phase.handleLeave(player));
    data.setSpectator(false);
    // (3) remove (thus reset) the match data
    player.getPlayerData().remove(data);
    // (4) restore entity data
    player.findOnline().ifPresent((entity) -> {
      PlayerSnapshot snapshot = data.getSnapshot();
      Preconditions.checkNotNull(snapshot, "Cannot restore entity (snapshot removed)");
      snapshot.restore(entity);
    });
    return true;
  }

  private void kickFirstNonPriorityPlayer() {
    // Kick other player due to player having priority
    SkywarsPlayer kick = getAudience().stream()
        .filter(Predicate.not(SkywarsPlayer::hasPriority))
        .findAny()
        .orElseThrow(() -> new LocalizableError("Could not find non-priority player",
            (lang) -> lang.get(MessageKeys.Match.PRIORITY_ERROR)));
    if (leave(kick))
      kick.sendFormattedMessage(MessageKeys.Match.PRIORITY_KICK);
  }

}
