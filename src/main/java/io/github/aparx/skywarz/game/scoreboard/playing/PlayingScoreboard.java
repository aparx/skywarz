package io.github.aparx.skywarz.game.scoreboard.playing;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.match.GameMatchState;
import io.github.aparx.skywarz.game.scoreboard.GameScoreboard;
import io.github.aparx.skywarz.game.scoreboard.ScoreboardContent;
import io.github.aparx.skywarz.game.scoreboard.SpecialScoreboard;
import io.github.aparx.skywarz.game.team.TeamMap;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-10 14:21
 * @since 1.0
 */
public class PlayingScoreboard extends GameScoreboard {

  public PlayingScoreboard(
      @NonNull GameMatchState state,
      @Nullable String name,
      @NonNull TickDuration intervalUpdate,
      @Nullable String initialTitle) {
    super(state, name, intervalUpdate, initialTitle);
  }

  public PlayingScoreboard(
      @NonNull GameMatchState state,
      @NonNull TickDuration updateInterval,
      @Nullable String initialTitle,
      @NonNull List<String> initialTemplateLines) {
    super(state, updateInterval, initialTitle, initialTemplateLines);
  }

  public PlayingScoreboard(
      @NonNull GameMatchState state,
      @Nullable String name,
      @NonNull TickDuration updateInterval,
      @Nullable String initialTitle,
      @NonNull List<String> initialTemplateLines) {
    super(state, name, updateInterval, initialTitle, initialTemplateLines);
  }


  @Override
  protected ScoreboardContent createContent(
      @NonNull SpecialScoreboard special, @NonNull GameMatch match, @Nullable Player viewer) {
    Scoreboard scoreboard = special.getScoreboard();
    Preconditions.checkNotNull(scoreboard, "Scoreboard is null");
    TeamMap teamMap = match.getTeamMap();
    teamMap.forEach((gameTeam) -> {
      String teamName = gameTeam.getTeamEnum().name();
      Team sbTeam = scoreboard.getTeam(teamName);
      if (sbTeam == null)
        sbTeam = scoreboard.registerNewTeam(teamName);
      final Team finalTeam = sbTeam;
      finalTeam.setColor(gameTeam.getTeamEnum().getChatColor());
      finalTeam.setCanSeeFriendlyInvisibles(false);
      finalTeam.getEntries().forEach(finalTeam::removeEntry);
      gameTeam.alive().forEach((member) -> finalTeam.addEntry(member.getName()));
      finalTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
    });
    return super.createContent(special, match, viewer);
  }
}
