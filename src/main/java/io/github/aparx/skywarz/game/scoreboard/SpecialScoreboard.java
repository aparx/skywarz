package io.github.aparx.skywarz.game.scoreboard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.WeakPlayerGroup;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-08 02:54
 * @since 1.0
 */
@Getter
public class SpecialScoreboard implements Listener {

  public static final String OBJECTIVE_NAME = "main";
  public static final String OBJECTIVE_CRITERIA = "dummy";

  @Getter(AccessLevel.NONE)
  private final WeakPlayerGroup viewers = new WeakPlayerGroup();

  private final @NonNull TickDuration updateInterval;
  private final @NonNull Function<SpecialScoreboard, @NonNull ScoreboardContent> contentFactory;

  private @Nullable Scoreboard scoreboard;
  private @Nullable Objective objective;

  private @Nullable ImmutableList<String> lastLines;

  private @Nullable BukkitTask task;

  public SpecialScoreboard(
      @NonNull TickDuration updateInterval,
      @NonNull Function<SpecialScoreboard, @NonNull ScoreboardContent> contentFactory) {
    Preconditions.checkNotNull(updateInterval, "Interval must not be null");
    Preconditions.checkNotNull(contentFactory, "Factory must not be null");
    this.updateInterval = updateInterval;
    this.contentFactory = contentFactory;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean show(@NonNull SkywarsPlayer viewer) {
    Preconditions.checkNotNull(viewer, "Viewer must not be null");
    if (!viewers.add(viewer)) return false;
    createScoreboardIfNeeded();
    Preconditions.checkNotNull(scoreboard, "Scoreboard is null");
    viewer.findOnline().ifPresent((player) -> player.setScoreboard(scoreboard));
    if (task != null) return true;
    task = Bukkit.getScheduler().runTaskTimer(Skywars.plugin(), () -> {
      // remove viewers from which the scoreboard differs
      viewers.forEach((x) -> x.findOnline()
          .filter((online) -> online.getScoreboard() != scoreboard)
          .ifPresent((__) -> viewers.remove(x)));
      this.render();
    }, 0, updateInterval.toTicks());
    // TODO this is not really needed since SkywarsPlayer is weakly referenced
    Bukkit.getPluginManager().registerEvent(PlayerQuitEvent.class, this, EventPriority.NORMAL,
        (listener, event) -> SkywarsPlayer
            .findPlayer(((PlayerQuitEvent) event).getPlayer())
            .ifPresent(this::remove),
        Skywars.plugin());
    return true;
  }

  @Synchronized
  @CanIgnoreReturnValue
  public boolean remove(@NonNull SkywarsPlayer viewer) {
    Preconditions.checkNotNull(viewer, "Viewer must not be null");
    if (!viewers.remove(viewer)) return false;
    viewer.findOnline().ifPresent((player) -> {
      ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
      if (scoreboardManager != null)
        player.setScoreboard(scoreboardManager.getMainScoreboard());
    });
    resetScoreboardIfNeeded();
    return true;
  }

  @Synchronized
  public void render() {
    if (resetScoreboardIfNeeded()) return;
    Preconditions.checkNotNull(scoreboard, "Scoreboard has not been created");
    Preconditions.checkNotNull(objective, "Objective has not been created");
    if (lastLines != null)
      lastLines.forEach((line) -> scoreboard.resetScores(line));
    if (scoreboard == null)
      createScoreboard();
    ScoreboardContent newContent = contentFactory.apply(this);
    Preconditions.checkNotNull(newContent, "Factory allocated null as ScoreboardContent");
    objective.setDisplayName(newContent.getTitle());
    final int length = newContent.length();
    ImmutableList.Builder<String> lastLineBuilder = ImmutableList.builderWithExpectedSize(length);
    newContent.forEach((line, index) -> {
      // Register a new score but also add it to the last line
      objective.getScore(String.valueOf(line)).setScore(length - index);
      lastLineBuilder.add(String.valueOf(line));
    });
    this.lastLines = lastLineBuilder.build();
  }

  @Synchronized
  @CanIgnoreReturnValue
  protected boolean createScoreboardIfNeeded() {
    if (scoreboard != null) return false;
    createScoreboard();
    return true;
  }

  @Synchronized
  @CanIgnoreReturnValue
  protected Scoreboard createScoreboard() {
    ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    Preconditions.checkNotNull(scoreboardManager, "ScoreboardManager is null");
    this.scoreboard = scoreboardManager.getNewScoreboard();
    this.objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, OBJECTIVE_CRITERIA, " ");
    this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    return this.scoreboard;
  }

  @Synchronized
  protected boolean resetScoreboardIfNeeded() {
    if (task == null || !viewers.isEmpty())
      return false;
    HandlerList.unregisterAll(this);
    task.cancel();
    task = null;
    scoreboard = null;
    objective = null;
    return true;
  }

  @Override
  public String toString() {
    return "SpecialScoreboard{" +
        "viewers=" + viewers +
        ", updateInterval=" + updateInterval +
        ", scoreboard=" + scoreboard +
        ", objective=" + objective +
        '}';
  }
}
