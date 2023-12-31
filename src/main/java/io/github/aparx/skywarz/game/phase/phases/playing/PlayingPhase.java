package io.github.aparx.skywarz.game.phase.phases.playing;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Magics;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.arena.settings.ArenaSettings;
import io.github.aparx.skywarz.game.kit.GameKit;
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
import io.github.aparx.skywarz.language.LazyVariableLookup;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.language.VariablePopulator;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
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

  public PlayingPhase(@NonNull GamePhaseCycler cycler) {
    super(GameMatchState.PLAYING, cycler,
        !Magics.isDevelopment()
            ? MainConfig.getInstance().getPhaseDuration(GameMatchState.PLAYING)
            : Magics.DEV_PLAYING_DURATION,
        TickDuration.of(TimeUnit.TICKS, 5));
    setListener(new PlayingListener(this));
  }

  public boolean hasProtectionPhase() {
    // TODO complexity cleanup
    return findMatch()
        .filter((match) -> match.getArena().getData().getSettings().getProtectionPhase())
        .isPresent();
  }

  public boolean isProtectionPhase() {
    return hasProtectionPhase() && !getTicker().hasElapsed(
        MainConfig.getInstance().getDurationProtection());
  }

  @Override
  public void handleJoin(SkywarsPlayer player) {
    PlayerMatchData data = player.getMatchData();
    data.setSpectator(true);
    // Handle spectator join
    player.findOnline().ifPresent((online) -> {
      GameSpectator.spawnAsSpectator(getMatch(), online);
      applyWorldView(getMatch(), online);
    });
    updateScoreboard(player);
  }

  @Override
  public void handleLeave(SkywarsPlayer player) {
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

  protected void updateScoreboard(SkywarsPlayer player) {
    getMatch().getScoreboardHandlers().getHandler(
            player.getMatchData().isSpectator()
                ? MatchScoreboard.PLAYING_DEAD
                : MatchScoreboard.PLAYING_ALIVE)
        .getOrCreateScoreboard(player)
        .show(player);
  }

  boolean wasProtecting = false;

  @Override
  protected void updateTick() {
    // (1) determine all teams that are alive
    if (evaluateGameEnd()) return;
    GameMatch match = getMatch();
    TimeTicker ticker = getTicker();
    if (ticker.isCycling(2))
      // update every 2nd tick due to performance
      match.getAudience().dead()
          .map(SkywarsPlayer::findOnline)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .filter((x) -> x.getGameMode() == GameMode.SPECTATOR && x.getSpectatorTarget() == null)
          .forEach((player) -> {
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);
          });

    boolean wasProtecting = this.wasProtecting;
    this.wasProtecting = isProtectionPhase();
    if (this.wasProtecting) {
      TickDuration duration = MainConfig.getInstance().getDurationProtection();
      long secsLeft = duration.toSeconds() - ticker.getElapsed(TimeUnit.SECONDS);
      if (ticker.isCycling(TimeUnit.SECONDS) && (
          (secsLeft <= 10 && secsLeft % 5 == 0)
              || secsLeft % 15 == 0 || secsLeft <= 3)) {
        LazyVariableLookup lookup = new LazyVariableLookup();
        VariablePopulator.addMatch(lookup, match, ArrayPath.of("match"));
        VariablePopulator.addFiniteTicker(lookup, ticker, duration, ArrayPath.of("time"));
        String message = Language.getInstance().substitute(
            MessageKeys.Match.COUNTDOWN_PROTECTION, lookup);
        match.getAudience().forEach((member) -> {
          member.sendMessage(message);
          SoundRecord.PROTECTION_TICK.play(member);
        });
      }
    } else if (wasProtecting) {
      LazyVariableLookup lookup = new LazyVariableLookup();
      VariablePopulator.addMatch(lookup, match, ArrayPath.of("match"));
      String message = Language.getInstance().substitute(
          MessageKeys.Match.PROTECTION_ENDED, lookup);
      match.getAudience().forEach((member) -> {
        member.sendMessage(message);
        SoundRecord.PROTECTION_END.play(member);
      });
    } else {
      long duration = getDuration().toSeconds();
      long secsLeft = duration - ticker.getElapsed(TimeUnit.SECONDS);
      if (ticker.isCycling(TimeUnit.SECONDS) && (ticker.isCycling(5, TimeUnit.MINUTES)
          || secsLeft % 60 == 0 || (secsLeft <= 60 && (secsLeft % 15 == 0 || secsLeft <= 10)))) {
        match.getAudience().forEach((member) -> {
          SoundRecord.TIMER_TICK.play(member);
          if (secsLeft <= 60)
            member.playTitle(StringUtils.SPACE,
                getColorForTimeLeft(secsLeft) + String.valueOf(secsLeft), 0, 30, 5);
        });
      }
    }
  }

  private void applyWorldView(GameMatch match, Player player) {
    ArenaSettings settings = match.getArena().getData().getSettings();
    WeatherType weather = settings.getWorldWeather();
    Integer time = settings.getWorldTime();
    if (weather != null)
      player.setPlayerWeather(weather);
    else player.resetPlayerWeather();
    if (time != null)
      player.setPlayerTime(time, false);
    else player.resetPlayerTime();
  }

  private ChatColor getColorForTimeLeft(long secsLeft) {
    if (secsLeft >= 60)
      return ChatColor.AQUA;
    if (secsLeft >= 45)
      return ChatColor.GREEN;
    if (secsLeft >= 30)
      return ChatColor.YELLOW;
    if (secsLeft >= 15)
      return ChatColor.GOLD;
    return ChatColor.RED;
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
    List<GameKit> kitPool = new ArrayList<>(match.getKits());
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
        GameKit kit = matchData.getKit();
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
        applyWorldView(match, online);
      }));
    });
  }

  void dequeueOnError(SkywarsPlayer player, Runnable runnable) {
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
  GameKit selectRandomKit(List<GameKit> kitPool, SkywarsPlayer player) {
    Preconditions.checkState(!kitPool.isEmpty(), "No kit available");
    GameKit kit = kitPool.get(ThreadLocalRandom.current().nextInt(0, kitPool.size()));
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
