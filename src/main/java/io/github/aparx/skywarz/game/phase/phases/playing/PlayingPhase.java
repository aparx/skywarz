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
import io.github.aparx.skywarz.game.arena.GameSettings;
import io.github.aparx.skywarz.game.kit.Kit;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.game.phase.features.Spectator;
import io.github.aparx.skywarz.game.team.Team;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.game.team.TeamMap;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
    super(MatchState.PLAYING, cycler, TickDuration.of(TimeUnit.HOURS, 15),
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
  public void handleJoin(SkywarsPlayer player) {
    PlayerMatchData data = player.getMatchData();
    data.setSpectator(true);
    // Handle spectator join
    player.findOnline().ifPresent((e) -> Spectator.spawnAsSpectator(getMatch(), e));
  }

  @Override
  public void handleLeave(SkywarsPlayer player) {
    player.findOnline().ifPresent((e) -> {
      Spectator.removeSpectator(getMatch(), e);
      findMatch().ifPresent((match) -> match.getAudience().dead()
          .map(SkywarsPlayer::findOnline)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach((other) -> e.showPlayer(Skywars.plugin(), other)));
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    Match match = getMatch();
    match.getAudience().online().forEach((player) -> dequeueOnError(player, () -> {
      PlayerMatchData matchData = player.getMatchData();
      player.findOnline().ifPresent((online) -> {
        PlayerSnapshot.ofReset(online).restore(online);
        if (matchData.isSpectator())
          Spectator.spawnAsSpectator(match, online);
      });
      if (!matchData.isSpectator() && !matchData.isInTeam())
        // Join player into team since they have not selected one
        Preconditions.checkState(getNextFreeTeam(match.getTeamMap()).add(player));
    }));
    spawnPlayers();
  }

  @Override
  protected void updateTick() {
    Match match = getMatch();
    // (1) determine all teams that are alive
    evaluateGameState();
  }

  protected void evaluateGameState() {
    int aliveCount = 0;
    Match match = getMatch();
    if (!match.isState(MatchState.PLAYING))
      return;
    TeamMap teamMap = match.getTeamMap();
    Iterator<Team> iterator = teamMap.iterator();
    List<Team> alive = new ArrayList<>();
    for (Team team; aliveCount < 2
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
      Bukkit.broadcastMessage("No team won!");
    } else if (aliveCount == 1 && !Magics.isDevelopment() /* TODO temporarily */) {
      getCycler().cycleNext();
      final Team won = alive.get(0);
      match.setWinner(won);
      Language language = Language.getInstance();
      String titleWin = language.substitute(MessageKeys.Match.TITLE_YOU_WON);
      String titleLost = language.substitute(MessageKeys.Match.TITLE_YOU_LOST);
      String titleTeam = language.get(MessageKeys.Match.TITLE_TEAM_WON)
          .substitute(won, ArrayPath.of("team"));
      String broadcast = language.get(MessageKeys.Match.TEAM_WON)
          .substitute(won, ArrayPath.of("team"));
      match.getAudience().forEach((player) -> {
        PlayerMatchData matchData = player.getMatchData();
        Team selfTeam = matchData.getTeam();
        boolean hasWon = won.equals(selfTeam);
        player.playTitle(hasWon ? titleWin : matchData.isInTeam() ? titleLost : titleTeam,
            StringUtils.SPACE, 5, (int) TickDuration.of(TimeUnit.SECONDS, 4).toTicks(), 15);
        player.sendMessage(" \n".repeat(3) + broadcast + " \n".repeat(4));
      });
    }
  }

  /**
   * Teleports all players to their according team spawns.
   * <p>If an error occurs when evaluating where a player should spawn, they are dequeued.
   */
  void spawnPlayers() {
    Match match = getMatch();
    List<Kit> kitPool = new ArrayList<>(match.getKits());
    Map<TeamEnum, SpawnGroup> spawns = new HashMap<>();
    match.getTeamMap().stream()
        .map(Team::getTeamEnum)
        .forEach((team) -> match.getArena().getData().getSpawns(team)
            .ifPresent((group) -> spawns.put(team, group.copy())));
    // O(n*m)
    match.getTeamMap().forEach((team) -> {
      SpawnGroup spawnGroup = spawns.get(team.getTeamEnum());
      team.forEach((player) -> dequeueOnError(player, () -> {
        PlayerMatchData matchData = player.getMatchData();
        // (1) select a random kit if the player has not chosen one
        if (!matchData.hasKit()) {
          selectRandomKit(kitPool, player);
          player.sendMessage(Language.getInstance()
              .get(MessageKeys.Match.KIT_ASSIGN)
              .substitute(player, ArrayPath.of("player")));
        }
        Kit kit = matchData.getKit();
        Preconditions.checkNotNull(kit, "Kit is still null");
        // (2) spawn the player at their respective team spawn
        Integer[] spawnIds = spawnGroup.toKeyArray();
        int spawnId = spawnIds[ThreadLocalRandom.current().nextInt(spawnIds.length)];
        Player online = player.getOnline();
        online.teleport(spawnGroup.get(spawnId));
        spawnGroup.remove(spawnId);
        SPAWN_SOUND.play(online);
        // (3) actually give the kit to the player
        kit.apply(online);
        // (4) apply the proper display name
        String displayName = team.getTeamEnum().getChatColor() + player.getName();
        online.setDisplayName(displayName);
        online.setPlayerListName(displayName);
      }));
    });
  }

  void dequeueOnError(SkywarsPlayer player, Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      findMatch().ifPresent((match) -> match.leave(player));
      player.sendMessage(Language.getInstance().substitute(MessageKeys.Match.ERROR_DEQUEUED));
    }
  }

  @CanIgnoreReturnValue
  Kit selectRandomKit(List<Kit> kitPool, SkywarsPlayer player) {
    Preconditions.checkState(!kitPool.isEmpty(), "No kit available");
    Kit kit = kitPool.get(ThreadLocalRandom.current().nextInt(0, kitPool.size()));
    player.getMatchData().setKit(kit);
    return kit;
  }

  /** Returns the next available team with the least number of players */
  @NonNull Team getNextFreeTeam(TeamMap teamMap) {
    Team filtered = null;
    int leastSize = -1;
    for (Team team : teamMap)
      if (team.hasSpace() && (leastSize < 0 || team.size() < leastSize)) {
        leastSize = team.size();
        filtered = team;
      }
    Preconditions.checkNotNull(filtered, "Could not find team");
    return filtered;
  }

}
