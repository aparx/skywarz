package io.github.aparx.skywarz.game.match;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsKey;
import io.github.aparx.skywarz.entity.data.types.PlayerStatsAccumulator;
import io.github.aparx.skywarz.events.match.MatchPointsCalculateEvent;
import io.github.aparx.skywarz.game.arena.reset.ArenaReset;
import io.github.aparx.skywarz.game.chest.ChestConfig;
import io.github.aparx.skywarz.game.chest.ChestHandler;
import io.github.aparx.skywarz.game.kit.SkywarsKit;
import io.github.aparx.skywarz.game.kit.SkywarsKitHandler;
import io.github.aparx.skywarz.game.scoreboard.MatchScoreboardHandlers;
import io.github.aparx.skywarz.game.team.GameTeam;
import io.github.aparx.skywarz.language.LocalizableError;
import io.github.aparx.skywarz.entity.GamePlayer;
import io.github.aparx.skywarz.entity.WeakPlayerGroup;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.game.arena.snapshot.ArenaSnapshot;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.game.team.TeamMap;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.Snowflake;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.util.NumberConversions;
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
@Setter
public class GameMatch implements Snowflake<UUID> {

  private final @NonNull UUID id;

  /** Returns a snapshot of an arena, so it is still valid for this match on allocation. */
  @Setter(AccessLevel.NONE)
  private volatile @NonNull ArenaSnapshot arena;

  @Getter(onMethod_ = {@Synchronized})
  @Setter(onMethod_ = {@Synchronized})
  private volatile @NonNull GameMatchState state = GameMatchState.SETUP;

  private volatile KeyValueSet<String, SkywarsKit> kits = SkywarsKitHandler.getInstance().getKits();

  private final GamePhaseCycler cycler = new GamePhaseCycler(this);

  private final GameMatchWatchTask watchTask = new GameMatchWatchTask(this);

  private final TeamMap teamMap = new TeamMap(this);

  private final ChestHandler chestHandler;

  private final MatchScoreboardHandlers scoreboardHandlers = new MatchScoreboardHandlers(this);

  private final WeakPlayerGroup audience = new WeakPlayerGroup() {
    @Override
    public boolean add(GamePlayer player) {
      if (!super.add(player)) return false;
      player.getMatchData().setMatch(GameMatch.this);
      return true;
    }

    @Override
    public boolean remove(Object o) {
      if (!super.remove(o)) return false;
      ((GamePlayer) o).getMatchData().setMatch(null);
      return true;
    }
  };

  private GameTeam winner;

  public GameMatch(@NonNull UUID id, @NonNull SkywarsArena arena) {
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
    SkywarsArena arenaSource = arena.getSource();
    Preconditions.checkNotNull(arenaSource, "Arena has become invalid");
    this.arena = new ArenaSnapshot(arenaSource);
    this.teamMap.createTeams();
    this.kits = SkywarsKitHandler.getInstance().createSnapshot();
    cycler.cycleJump(GameMatchState.IDLE);

    getWatchTask().start();
  }

  /** Called by the {@code MatchManager} when this match has been removed. */
  public void notifyRemoval() {
    try {
      // Stops the lobby
      getWatchTask().stop();
      getAudience().forEach(this::leave);
      getChestHandler().reset();
      Objects.requireNonNull(arena.getSource()).getSignHandler().update();
    } finally {
      // enforce map reset
      Optional.ofNullable(getArena().getSource())
          .map(SkywarsArena::getReset)
          .ifPresent(ArenaReset::reset);
    }
  }

  @Synchronized
  public boolean isState(GameMatchState state) {
    return this.state == state;
  }


  public int getTeamSize() {
    return getArena().getData().getSettings().getTeamSize();
  }

  public int getMinPlayerCount() {
    return SkywarsArena.getMinPlayerCount(getArena().getData().getSettings());
  }

  public int getMaxPlayerCount() {
    return SkywarsArena.getMaxPlayerCount(getArena().getData().getSettings(), getTeamMap().size());
  }

  @CanIgnoreReturnValue
  public boolean join(@NonNull GamePlayer player) {
    Preconditions.checkNotNull(player, "Player must not be null");
    PlayerMatchData data = player.getMatchData();
    if (audience.contains(player)
        || data.isInMatch()
        || !getState().isJoinable())
      return false;
    boolean isExceedingPlayerSize = isState(GameMatchState.IDLE)
        && getAudience().size() >= getMaxPlayerCount();
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
      Objects.requireNonNull(arena.getSource()).getSignHandler().update();
      return true;
    } catch (Exception e) {
      leave(player);
      player.sendMessage(Language.getInstance().substitute(MessageKeys.Match.JOIN_ERROR));
      return false;
    }
  }

  @CanIgnoreReturnValue
  public boolean leave(@NonNull GamePlayer player) {
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
    Objects.requireNonNull(arena.getSource()).getSignHandler().update();
    return true;
  }

  /**
   * Adds the during match accumulated statistics from {@code player} to the database, and
   * manages the increment of number of matches played, total wins and also the points.
   *
   * @param player the player to which to apply the stats to
   */
  public void applyStats(@NonNull GamePlayer player) {
    PlayerMatchData matchData = player.getMatchData();
    boolean hasWon = matchData.isInTeam() && Objects.equals(getWinner(), matchData.getTeam());
    PlayerStatsAccumulator statistics = player.getMatchData().getStatistics();
    Preconditions.checkNotNull(statistics, "Stats accumulator is null");
    statistics.increment(PlayerStatsKey.PLAYED);
    if (hasWon) statistics.increment(PlayerStatsKey.WON);
    statistics.increment(PlayerStatsKey.POINTS, calculatePoints(player, statistics));
    Skywars.getInstance().getDatabase().queue((database) -> {
      database.getStatsManager().apply(statistics);
    });
  }

  public int calculatePoints(GamePlayer player, PlayerStatsAccumulator accumulator) {
    final int weightOfWin = 10;
    MatchPointsCalculateEvent event = new MatchPointsCalculateEvent(this, player, accumulator);
    event.setPoints(NumberConversions.ceil(2 * accumulator.findGet(PlayerStatsKey.KILLS)
        + weightOfWin * accumulator.findGet(PlayerStatsKey.WON)
        - .5 * weightOfWin * accumulator.findGet(PlayerStatsKey.PLAYED)
        - 3 * accumulator.findGet(PlayerStatsKey.DEATHS)));
    Bukkit.getPluginManager().callEvent(event);
    return event.getPoints();
  }

  private void kickFirstNonPriorityPlayer() {
    // Kick other player due to player having priority
    GamePlayer kick = getAudience().stream()
        .filter(Predicate.not(GamePlayer::hasPriority))
        .findAny()
        .orElseThrow(() -> new LocalizableError("Could not find non-priority player",
            (lang) -> lang.get(MessageKeys.Match.PRIORITY_ERROR)));
    if (leave(kick))
      kick.sendFormattedMessage(MessageKeys.Match.PRIORITY_KICK);
  }

}
