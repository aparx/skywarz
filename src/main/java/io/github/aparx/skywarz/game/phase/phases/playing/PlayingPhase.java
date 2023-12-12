package io.github.aparx.skywarz.game.phase.phases.playing;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Magics;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.GamePlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.arena.GameSettings;
import io.github.aparx.skywarz.game.kit.SkywarsKit;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.match.GameMatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.game.phase.features.GameSpectator;
import io.github.aparx.skywarz.game.scoreboard.MatchScoreboard;
import io.github.aparx.skywarz.game.team.GameTeam;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.game.team.TeamMap;
import io.github.aparx.skywarz.handler.MainConfig;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:58
 * @since 1.0
 */
public class PlayingPhase extends GamePhase {

  private static final SoundRecord SPAWN_SOUND =
      SoundRecord.of(Sound.ENTITY_PLAYER_LEVELUP, .25f, .75f);

  // TODO move to game settings
  private static final TickDuration PROTECTION_PHASE_TIME = TickDuration.of(TimeUnit.SECONDS, 60);

  public PlayingPhase(@NonNull GamePhaseCycler cycler) {
    super(GameMatchState.PLAYING, cycler,
        !Magics.isDevelopment()
            ? MainConfig.getInstance().getPhaseDuration(GameMatchState.PLAYING)
            : Magics.DEV_PLAYING_DURATION,
        TickDuration.of(TimeUnit.TICKS, 5));
    setListener(new PlayingListener(this));
  }

  public boolean hasProtectionPhase() {
    return findMatch()
        .map((match) -> match.getArena().getData().getSettings().getFlags())
        .filter(GameSettings.Flag.PROTECTION_PHASE::isFlagged)
        .isPresent();
  }

  public boolean isProtectionPhase() {
    return hasProtectionPhase() && getTicker().hasElapsed(PROTECTION_PHASE_TIME);
  }

  @Override
  public void handleJoin(GamePlayer player) {
    PlayerMatchData data = player.getMatchData();
    data.setSpectator(true);
    // Handle spectator join
    player.findOnline().ifPresent((e) -> GameSpectator.spawnAsSpectator(getMatch(), e));
    updateScoreboard(player);
  }

  @Override
  public void handleLeave(GamePlayer player) {
    findMatch().ifPresent((match) -> {
      match.applyStats(player);
      player.findOnline().ifPresent((e) -> GameSpectator.removeSpectator(getMatch(), e));
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    GameMatch match = getMatch();
    Objects.requireNonNull(match.getArena().getSource()).getReset().capture();
    match.getAudience().online().forEach((player) -> dequeueOnError(player, () -> {
      PlayerMatchData matchData = player.getMatchData();
      player.findOnline().ifPresent((online) -> {
        PlayerSnapshot.ofReset(online).restore(online);
        if (matchData.isSpectator())
          GameSpectator.spawnAsSpectator(match, online);
      });
      if (!matchData.isSpectator() && !matchData.isInTeam())
        // Join player into team since they have not selected one
        Preconditions.checkState(getNextFreeTeam(match.getTeamMap()).add(player));
      updateScoreboard(player);
    }));
    spawnPlayers();
  }

  protected void updateScoreboard(GamePlayer player) {
    getMatch().getScoreboardHandlers().getHandler(
            player.getMatchData().isSpectator()
                ? MatchScoreboard.PLAYING_DEAD
                : MatchScoreboard.PLAYING_ALIVE)
        .getOrCreateScoreboard(player)
        .show(player);
  }

  @Override
  protected void updateTick() {
    // (1) determine all teams that are alive
    if (evaluateGameEnd()) return;
    GameMatch match = getMatch();
    TimeTicker ticker = getTicker();
    long duration = getDuration().toSeconds();
    long secsLeft = duration - ticker.getElapsed(TimeUnit.SECONDS);
    if (ticker.isCycling(TimeUnit.SECONDS) && (ticker.isCycling(5, TimeUnit.MINUTES)
        || secsLeft % 60 == 0 || (secsLeft <= 60 && (secsLeft % 15 == 0 || secsLeft <= 10)))) {
      match.getAudience().forEach((member) -> {
        SoundRecord.TIMER_TICK.play(member);
        if (secsLeft <= 60)
          member.playActionbar(ChatColor.RED + String.valueOf(secsLeft));
      });
    }
  }

  @CanIgnoreReturnValue
  protected boolean evaluateGameEnd() {
    int aliveCount = 0;
    GameMatch match = getMatch();
    if (!match.isState(GameMatchState.PLAYING))
      return false;
    if (match.getWinner() == null) {
      TeamMap teamMap = match.getTeamMap();
      Iterator<GameTeam> iterator = teamMap.iterator();
      List<GameTeam> alive = new ArrayList<>();
      for (GameTeam team; aliveCount < 2
          && iterator.hasNext()
          && (team = iterator.next()) != null; )
        if (team.alive().findAny().isPresent()) {
          alive.add(team);
          ++aliveCount;
        }
      if (aliveCount == 0) {
        // no player existing anymore, thus cancel
        stop(StopReason.UNKNOWN);
        Skywars.getInstance().getMatchManager().remove(match);
        return true;
      } else if (aliveCount == 1 && Magics.GAME_WINNABLE) {
        match.setWinner(alive.get(0));
      }
    }
    if (match.getWinner() != null)
      // moved out of the block above, for API accessibility
      return getCycler().cycleNext().isPresent();
    return false;
  }

  /**
   * Teleports all players to their according team spawns.
   * <p>If an error occurs when evaluating where a player should spawn, they are dequeued.
   */
  void spawnPlayers() {
    GameMatch match = getMatch();
    List<SkywarsKit> kitPool = new ArrayList<>(match.getKits());
    Map<TeamEnum, SpawnGroup> spawns = new HashMap<>();
    match.getTeamMap().stream()
        .map(GameTeam::getTeamEnum)
        .forEach((team) -> match.getArena().getData().getSpawns(team)
            .ifPresent((group) -> spawns.put(team, group.copy())));
    // O(n*m)
    match.getTeamMap().forEach((team) -> {
      SpawnGroup spawnGroup = spawns.get(team.getTeamEnum());
      boolean removeSpawns = team.size() <= spawnGroup.size();
      team.forEach((player) -> dequeueOnError(player, () -> {
        PlayerMatchData matchData = player.getMatchData();
        // (1) select a random kit if the player has not chosen one
        if (!matchData.hasKit()) {
          selectRandomKit(kitPool, player);
          player.sendMessage(Language.getInstance()
              .get(MessageKeys.Match.KIT_ASSIGN)
              .substitute(player, ArrayPath.of("player")));
        }
        SkywarsKit kit = matchData.getKit();
        Preconditions.checkNotNull(kit, "Kit is still null");
        // (2) spawn the player at their respective team spawn
        Integer[] spawnIds = spawnGroup.toKeyArray();
        int spawnId = spawnIds[ThreadLocalRandom.current().nextInt(spawnIds.length)];
        Player online = player.getOnline();
        online.teleport(spawnGroup.get(spawnId));
        if (removeSpawns) spawnGroup.remove(spawnId);
        SPAWN_SOUND.play(online);
        // (3) actually give the kit to the player
        kit.apply(online);
        // (4) apply the proper display name
        String displayName = team.getTeamEnum().getChatColor() + player.getName();
        //  this causes IllegalStateException: Missing key in ResourceKey for some reason?
        // online.setDisplayName(team.getTeamEnum().getChatColor() + player.getName());
        online.setPlayerListName(displayName);
      }));
    });
  }

  void dequeueOnError(GamePlayer player, Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      if (Magics.isDevelopment())
        Skywars.logger().log(Level.WARNING, "Error in playing phase", e);
      findMatch().ifPresent((match) -> match.leave(player));
      player.sendMessage(Language.getInstance().substitute(MessageKeys.Match.ERROR_DEQUEUED));
    }
  }

  @CanIgnoreReturnValue
  SkywarsKit selectRandomKit(List<SkywarsKit> kitPool, GamePlayer player) {
    Preconditions.checkState(!kitPool.isEmpty(), "No kit available");
    SkywarsKit kit = kitPool.get(ThreadLocalRandom.current().nextInt(0, kitPool.size()));
    player.getMatchData().setKit(kit);
    return kit;
  }

  /** Returns the next available team with the least number of players */
  @NonNull GameTeam getNextFreeTeam(TeamMap teamMap) {
    GameTeam filtered = null;
    int leastSize = -1;
    for (GameTeam team : teamMap)
      if (team.hasSpace() && (leastSize < 0 || team.size() < leastSize)) {
        leastSize = team.size();
        filtered = team;
      }
    Preconditions.checkNotNull(filtered, "Could not find team");
    return filtered;
  }

}
