package io.github.aparx.skywarz.game.phase.phases.idle;

import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.WeakGroupAudience;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.entity.snapshot.PlayerSnapshot;
import io.github.aparx.skywarz.game.item.items.LeaveItem;
import io.github.aparx.skywarz.game.item.items.idle.KitSelectorItem;
import io.github.aparx.skywarz.game.item.items.idle.TeamSelectorItem;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.phase.GamePhase;
import io.github.aparx.skywarz.game.phase.GamePhaseCycler;
import io.github.aparx.skywarz.game.phase.features.LevelAnimator;
import io.github.aparx.skywarz.game.phase.phases.LobbyPhaseListener;
import io.github.aparx.skywarz.game.scoreboard.MatchScoreboard;
import io.github.aparx.skywarz.game.team.Team;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.LazyVariableLookup;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.language.ValueMapPopulators;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import io.github.aparx.skywarz.utils.tick.Ticker;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 01:58
 * @since 1.0
 */
public class IdlePhase extends GamePhase {

  private final Ticker trigger;

  private boolean lastMinimumPlayers;
  private int lastPlayerSize;

  private long secsLeft;

  public IdlePhase(@NonNull GamePhaseCycler cycler) {
    super(MatchState.IDLE, cycler,
        TickDuration.of(TimeUnit.SECONDS, 3),
        /* Update all two ticks to save performance */
        TickDuration.of(TimeUnit.TICKS, 2));
    trigger = new TimeTicker(getInterval());
    setListener(new LobbyPhaseListener(this));
  }

  @Override
  public void handleJoin(SkywarsPlayer player) {
    // Manage entity
    Player entity = player.getOnline();
    Match match = getMatch();
    // show IDLE scoreboard
    match.getScoreboardHandlers()
        .getHandler(MatchScoreboard.IDLE)
        .getOrCreateScoreboard(player)
        .show(player);
    PlayerSnapshot.ofReset(entity,
        match.getArena().getData().getLobby(),
        GameMode.ADVENTURE).restore(entity);
    entity.getInventory().setItem(LeaveItem.SLOT, Skywars.getInstance()
        .getGameItemManager()
        .getItems()
        .require(LeaveItem.class)
        .create(match, entity));

    Skywars.getInstance()
        .getGameItemManager()
        .getItems()
        .require(TeamSelectorItem.class)
        .give(match, entity);

    Skywars.getInstance()
        .getGameItemManager()
        .getItems()
        .require(KitSelectorItem.class)
        .give(match, entity);
  }

  @Override
  protected void updateTick() {
    Ticker ticker = getTicker();
    Match match = getMatch();
    WeakGroupAudience<SkywarsPlayer> players = match.getAudience();
    final int minPlayers = match.getMinPlayerSize();
    final int playerSize = players.size();
    int missingPlayerAmount = minPlayers - playerSize;
    boolean lastMinimumPlayers = this.lastMinimumPlayers;
    boolean hasMinimumPlayers = missingPlayerAmount <= 0;
    this.lastMinimumPlayers = hasMinimumPlayers;
    if (!hasMinimumPlayers) ticker.set(-1);
    trigger.tick();
    if ((ticker.getElapsed() <= 0 && trigger.isCycling(TimeUnit.SECONDS))
        || getTicker().isCycling(TimeUnit.SECONDS)) {
      int lastPlayerSize = this.lastPlayerSize;
      this.lastPlayerSize = playerSize;
      if (hasMinimumPlayers) {
        long durationSecs = getDuration().toSeconds();
        long elapsed = ticker.getElapsed(TimeUnit.SECONDS);
        secsLeft = durationSecs - elapsed;
        if (((elapsed == 0 || secsLeft <= 3)
            || (secsLeft <= 20 && secsLeft % 5 == 0)
            || (secsLeft <= 60 && secsLeft % 15 == 0)
            || secsLeft % 30 == 0)) {
          LazyVariableLookup lookup = new LazyVariableLookup();
          ValueMapPopulators.populateMatch(lookup, match, ArrayPath.of("match"));
          players.forEach((player) -> {
            player.sendFormattedMessage(MessageKeys.Match.BROADCAST_START, lookup);
            SoundRecord.TIMER_TICK.play(player);
          });
        }
      } else if (playerSize != lastPlayerSize || trigger.isCycling(30, TimeUnit.SECONDS))
        players.sendMessage(Language.getInstance()
            .get(MessageKeys.Match.BROADCAST_REQUIRE)
            .substitute(match, ArrayPath.of("match")));
      if (trigger.isCycling(3, TimeUnit.TICKS))
        players.forEach((player) -> {
          PlayerMatchData matchData = player.getMatchData();
          Team team = matchData.getTeam();
          if (team != null && matchData.isInTeam()) {
            TeamEnum e = team.getTeamEnum();
            player.playActionbar(e.getChatColor() + "Team " + Language.getInstance().getTeamName(e));
          }
        });
    }
    if (hasMinimumPlayers) LevelAnimator.animate(this, (int) secsLeft);
    else if (lastMinimumPlayers) LevelAnimator.animate(this, 0);
  }
}