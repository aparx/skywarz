package io.github.aparx.skywarz.game.phase.phases.done;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.item.items.LeaveItem;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.game.phase.features.LevelAnimator;
import io.github.aparx.skywarz.game.phase.features.Spectator;
import io.github.aparx.skywarz.handler.MainConfig;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
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
    super(MatchState.DONE, cycler,
        TickDuration.of(TimeUnit.SECONDS, 15),
        TickDuration.of(TimeUnit.TICKS, 2));
    setListener(new DoneListener(this));
  }

  @Override
  public void handleJoin(SkywarsPlayer player) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void onStart() {
    super.onStart();
    Match match = getMatch();
    match.getAudience().entity().forEach((player) -> {
      Spectator.removeSpectator(match, player);
      PlayerSnapshot.ofReset(player, GameMode.ADVENTURE).restore(player);
      player.teleport(match.getArena().getData().getLobby());
      player.getInventory().setItem(LeaveItem.SLOT,
          Skywars.getInstance()
              .getGameItemManager()
              .getItems()
              .require(LeaveItem.class)
              .create(match, player));
    });
  }

  @Override
  protected void onStop(StopReason reason) {
    super.onStop(reason);
    findMatch().ifPresent((match) -> Skywars.getInstance().getMatchManager().remove(match));
  }

  @Override
  protected void updateTick() {
    Match match = getMatch();
    long duration = getDuration().toSeconds();
    long secsLeft = duration - getTicker().getElapsed(TimeUnit.SECONDS);
    if (getTicker().isCycling(TimeUnit.SECONDS)) {
      if (secsLeft != duration && (secsLeft % 5 == 0 || secsLeft <= 3))
        match.getAudience().sendFormattedMessage(
            MessageKeys.Match.BROADCAST_CLOSING,
            Map.of("time", secsLeft));
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
    Match match = getMatch();
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
