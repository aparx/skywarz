package io.github.aparx.skywarz.game.team;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.match.SkywarsMatch;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 12:14
 * @since 1.0
 */
@Getter
public final class TeamMap implements Iterable<GameTeam> {

  private final WeakReference<SkywarsMatch> match;

  private @NonNull Map<TeamEnum, GameTeam> teams = new HashMap<>();

  public TeamMap(@NonNull SkywarsMatch match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    this.match = new WeakReference<>(match);
  }

  public @NonNull SkywarsMatch getMatch() {
    SkywarsMatch match = this.match.get();
    Preconditions.checkState(match != null, "Match has become invalid");
    return match;
  }

  public int size() {
    return teams.size();
  }

  public void createTeams() {
    final SkywarsMatch match = getMatch();
    this.teams = match.getArena().getData().getSpawns().entrySet().stream()
        .filter((entry) -> !entry.getValue().isEmpty())
        .map((entry) -> TeamEnum.valueOf(entry.getKey()))
        .map((teamEnum) -> new GameTeam(match, teamEnum))
        .collect(Collectors.toMap(GameTeam::getTeamEnum, Function.identity()));
  }

  public Optional<GameTeam> findTeam(@NonNull TeamEnum team) {
    Preconditions.checkNotNull(team, "TeamEnum must not be null");
    return Optional.ofNullable(teams.get(team));
  }

  public @NonNull GameTeam getTeam(@NonNull TeamEnum team) {
    Preconditions.checkNotNull(team, "TeamEnum must not be null");
    return findTeam(team).orElseThrow();
  }

  public @NonNull Collection<@NonNull GameTeam> getTeams() {
    return teams.values();
  }

  public @NonNull Set<@NonNull TeamEnum> getEnums() {
    return teams.keySet();
  }

  @Override
  public @NonNull Iterator<GameTeam> iterator() {
    return teams.values().iterator();
  }

  public @NonNull Stream<@NonNull GameTeam> stream() {
    return teams.values().stream();
  }
}
