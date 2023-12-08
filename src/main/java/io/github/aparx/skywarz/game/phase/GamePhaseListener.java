package io.github.aparx.skywarz.game.phase;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.match.Match;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-07 10:40
 * @since 1.0
 */
@Getter
public abstract class GamePhaseListener<T extends GamePhase> implements Listener {

  private final @NonNull T phase;

  private final Function<SkywarsPlayer, Optional<Match>> playerFilter = (player) -> {
    Optional<Match> matchQuery = getPhase().findMatch();
    if (matchQuery.isEmpty()) return Optional.empty();
    Match match = matchQuery.get();
    return player.getPlayerData()
        .find(PlayerMatchData.class)
        .filter(PlayerMatchData::isInMatch)
        .filter((data) -> match.equals(data.getMatch()))
        .map((x) -> match);
  };

  public GamePhaseListener(@NonNull T phase) {
    Preconditions.checkNotNull(phase, "Phase must not be null");
    this.phase = phase;
  }

  public void load() {
    Bukkit.getPluginManager().registerEvents(this, Skywars.plugin());
  }

  public void unload() {
    HandlerList.unregisterAll(this);
  }

  protected Optional<Match> filterMatchFromPlayer(Player entity) {
    return SkywarsPlayer.findPlayer(entity).flatMap(playerFilter);
  }

  protected Optional<Match> filterMatch(Match match) {
    return Optional.ofNullable(match).filter((data) -> {
      return Objects.equals(data, getPhase().findMatch().orElse(null));
    });
  }
}
