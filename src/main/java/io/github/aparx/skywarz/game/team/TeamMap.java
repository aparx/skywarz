package io.github.aparx.skywarz.game.team;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.match.Match;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 12:14
 * @since 1.0
 */
@Getter
public final class TeamMap implements Iterable<Team> {

  private final WeakReference<Match> match;

  private @NonNull Map<TeamEnum, Team> teams = new HashMap<>();

  public TeamMap(@NonNull Match match) {
    Preconditions.checkNotNull(match, "Match must not be null");
    this.match = new WeakReference<>(match);
  }

  public @NonNull Match getMatch() {
    Match match = this.match.get();
    Preconditions.checkState(match != null, "Match has become invalid");
    return match;
  }

  public int size() {
    return teams.size();
  }

  public void createTeams() {
    final Match match = getMatch();
    this.teams = match.getArena().getData().getSpawns().entrySet().stream()
        .filter((entry) -> !entry.getValue().isEmpty())
        .map((entry) -> TeamEnum.valueOf(entry.getKey()))
        .map((teamEnum) -> new Team(match, teamEnum))
        .collect(Collectors.toMap(Team::getTeamEnum, Function.identity()));
  }

  public Optional<Team> findTeam(@NonNull TeamEnum team) {
    Preconditions.checkNotNull(team, "TeamEnum must not be null");
    return Optional.ofNullable(teams.get(team));
  }

  public @NonNull Team getTeam(@NonNull TeamEnum team) {
    Preconditions.checkNotNull(team, "TeamEnum must not be null");
    return findTeam(team).orElseThrow();
  }

  public @NonNull Collection<@NonNull Team> getTeams() {
    return teams.values();
  }

  public @NonNull Set<@NonNull TeamEnum> getEnums() {
    return teams.keySet();
  }

  @Override
  public @NonNull Iterator<Team> iterator() {
    return teams.values().iterator();
  }
}
