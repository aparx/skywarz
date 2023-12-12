package io.github.aparx.skywarz.database.stats;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsKey;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsAccumulator;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 08:34
 * @since 1.0
 */
@Getter
@Setter
@DatabaseTable(tableName = "stats")
public class PlayerDatabaseStats {

  @DatabaseField(id = true)
  private UUID id;

  @DatabaseField
  private int points;

  @DatabaseField
  private int kills;

  @DatabaseField
  private int deaths;

  @DatabaseField
  private int matchesPlayed;

  @DatabaseField
  private int matchesWon;

  public PlayerStatsAccumulator accumulate() {
    EnumMap<PlayerStatsKey, AtomicInteger> map = new EnumMap<>(PlayerStatsKey.class);
    map.put(PlayerStatsKey.POINTS, new AtomicInteger(getPoints()));
    map.put(PlayerStatsKey.KILLS, new AtomicInteger(getKills()));
    map.put(PlayerStatsKey.DEATHS, new AtomicInteger(getDeaths()));
    map.put(PlayerStatsKey.PLAYED, new AtomicInteger(getMatchesPlayed()));
    map.put(PlayerStatsKey.WON, new AtomicInteger(getMatchesWon()));
    return new PlayerStatsAccumulator(getId(), map);
  }

  public void add(PlayerStatsAccumulator accumulator) {
    this.points = Math.max(points + accumulator.findGet(PlayerStatsKey.POINTS), 0);
    this.kills = Math.max(kills + accumulator.findGet(PlayerStatsKey.KILLS), 0);
    this.deaths = Math.max(deaths + accumulator.findGet(PlayerStatsKey.DEATHS), 0);
    this.matchesPlayed = Math.max(matchesPlayed + accumulator.findGet(PlayerStatsKey.PLAYED), 0);
    this.matchesWon = Math.max(matchesWon + accumulator.findGet(PlayerStatsKey.WON), 0);
  }

  @Override
  public String toString() {
    return "PlayerStats{" +
        "id=" + id +
        ", kills=" + kills +
        ", deaths=" + deaths +
        ", matchesPlayed=" + matchesPlayed +
        ", matchesWon=" + matchesWon +
        '}';
  }
}
