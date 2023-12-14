package io.github.aparx.skywarz.game.phase.phases.done;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.item.items.LeaveItem;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.match.GameMatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.game.phase.features.LevelAnimator;
import io.github.aparx.skywarz.game.phase.features.GameSpectator;
import io.github.aparx.skywarz.game.scoreboard.MatchScoreboard;
import io.github.aparx.skywarz.game.team.GameTeam;
import io.github.aparx.skywarz.handler.MainConfig;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.LazyVariableLookup;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.language.VariablePopulator;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:58
 * @since 1.0
 */
public class DonePhase extends GamePhase {

  public DonePhase(@NonNull GamePhaseCycler cycler) {
    super(GameMatchState.DONE, cycler,
        MainConfig.getInstance().getPhaseDuration(GameMatchState.DONE),
        TickDuration.of(TimeUnit.TICKS, 2));
    setListener(new DoneListener(this));
  }

  @Override
  public void handleJoin(SkywarsPlayer player) {
    throw new IllegalStateException("Cannot join during DONE state");
  }

  @Override
  protected void onStart() {
    super.onStart();
    GameMatch match = getMatch();
    GameTeam won = match.getWinner();
    final boolean hasWinner = won != null;
    Language language = Language.getInstance();
    String titleWin = language.substitute(MessageKeys.Match.TITLE_YOU_WON);
    String titleLost = language.substitute(MessageKeys.Match.TITLE_YOU_LOST);
    String titleTeam = !hasWinner ? null : language
        .get(MessageKeys.Match.TITLE_TEAM_WON)
        .substitute(won, ArrayPath.of("team"));
    String broadcast = !hasWinner ? null : language
        .get(MessageKeys.Match.TEAM_WON)
        .substitute(won, ArrayPath.of("team"));
    match.getAudience().entity().forEach((entity) -> {
      SkywarsPlayer player = SkywarsPlayer.getPlayer(entity);
      if (!player.getMatchData().isSpectator())
        match.applyStats(player); // apply stats since player is not dead already
      GameSpectator.removeSpectator(match, entity);
      PlayerSnapshot.ofReset(entity, GameMode.ADVENTURE).restore(entity);
      entity.teleport(match.getArena().getData().getLobby());
      entity.getInventory().setItem(LeaveItem.SLOT,
          Skywars.getInstance()
              .getItemManager()
              .getItems()
              .require(LeaveItem.class)
              .create(match, entity));
      PlayerMatchData matchData = player.getMatchData();
      boolean hasWon = hasWinner && won.equals(matchData.getTeam());
      entity.sendTitle(hasWon ? titleWin : matchData.isInTeam() ? titleLost :
              hasWinner ? titleTeam : titleLost,
          StringUtils.SPACE, 5, (int) TickDuration.of(TimeUnit.SECONDS, 4).toTicks(), 15);
      if (hasWinner)
        entity.sendMessage(" \n".repeat(3) + broadcast + " \n".repeat(4));

      match.getScoreboardHandlers()
          .getHandler(MatchScoreboard.DONE)
          .getOrCreateScoreboard(player)
          .show(player);

    });
  }

  @Override
  protected void onStop(StopReason reason) {
    super.onStop(reason);
    findMatch().ifPresent((match) -> Skywars.getInstance().getMatchManager().remove(match));
  }

  @Override
  protected void updateTick() {
    GameMatch match = getMatch();
    long duration = getDuration().toSeconds();
    long secsLeft = duration - getTicker().getElapsed(TimeUnit.SECONDS);
    if (getTicker().isCycling(TimeUnit.SECONDS)) {
      if (secsLeft != duration && (secsLeft % 5 == 0 || secsLeft <= 3)) {
        LazyVariableLookup lookup = new LazyVariableLookup();
        VariablePopulator.addMatch(lookup, match, ArrayPath.of("match"));
        match.getAudience().sendFormattedMessage(MessageKeys.Match.COUNTDOWN_CLOSING, lookup);
      }
    }
    LevelAnimator.animate(this, (int) secsLeft);
    if (match.getAudience().isEmpty())
      getCycler().cycleNext();

    if (match.getWinner() != null && getTicker().isCycling(10, TimeUnit.TICKS)) {
      MainConfig mainConfig = MainConfig.getInstance();
      if (mainConfig.isCelebrationEnabled())
        spawnFireworks(mainConfig, match.getWinner().getTeamEnum().getColor());
    }
  }

  private void spawnFireworks(MainConfig mainConfig, Color color) {
    GameMatch match = getMatch();
    Location location = match.getArena().getData().getLobby();
    Location mutable = location.clone();
    Random random = ThreadLocalRandom.current();
    double radius = mainConfig.getCelebrationFireworkRadius();
    double height = mainConfig.getCelebrationFireworkHeight();
    for (int i = 0; i < 2; ++i) {
      double xOffset = (-0.5F + random.nextFloat() * 1.5F) * radius;
      double zOffset = (-0.5F + random.nextFloat() * 1.5F) * radius;
      double yOffset = height + random.nextFloat() * radius;
      mutable.setX(location.getX() + xOffset);
      mutable.setY(location.getY() + yOffset);
      mutable.setZ(location.getZ() + zOffset);
      spawnFirework(color, mutable);
    }
  }

  private void spawnFirework(Color color, Location location) {
    World world = location.getWorld();
    Preconditions.checkNotNull(world);
    Firework firework = world.spawn(location, Firework.class);
    FireworkMeta meta = firework.getFireworkMeta();
    meta.addEffect(FireworkEffect.builder()
        .withColor(color)
        .withFade(Color.WHITE)
        .withTrail()
        .build());
    firework.setFireworkMeta(meta);
    firework.detonate();
  }

}
