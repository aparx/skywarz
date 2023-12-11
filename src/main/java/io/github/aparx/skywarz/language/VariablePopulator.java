package io.github.aparx.skywarz.language;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.stats.PlayerStatsKey;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.entity.data.types.PlayerStatsAccumulator;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.arena.GameSettings;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import io.github.aparx.skywarz.game.kit.SkywarsKit;
import io.github.aparx.skywarz.game.match.SkywarsMatch;
import io.github.aparx.skywarz.game.match.SkywarsMatchManager;
import io.github.aparx.skywarz.game.match.SkywarsMatchState;
import io.github.aparx.skywarz.game.team.GameTeam;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.utils.tick.Ticker;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Locale;
import java.util.function.Predicate;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 14:33
 * @since 1.0
 */
@UtilityClass
public final class VariablePopulator {

  public static void addPlayer(
      @NonNull LazyVariableLookup lookup,
      @NonNull OfflinePlayer entity,
      @NonNull ArrayPath prefix,
      @Nullable Object nullValue) {
    lookup.set(prefix.add("name"), entity.getName());
    SkywarsPlayer.findPlayer(entity.getUniqueId())
        .map(SkywarsPlayer::getPlayerData)
        .flatMap((storage) -> storage.find(PlayerMatchData.class))
        .ifPresent((data) -> {
          addKit(lookup, data.getKit(), prefix.add("kit"), nullValue);
          addTeam(lookup, data.getTeam(), prefix.add("team"), nullValue);
          addStats(lookup, data.getStatistics(), prefix.add(ArrayPath.of("match", "stats")));
        });
  }

  public static void addPlayer(
      @NonNull LazyVariableLookup lookup,
      @NonNull Player entity,
      @NonNull ArrayPath prefix,
      @Nullable Object nullValue) {
    Preconditions.checkNotNull(entity, "Entity must not be null");
    lookup.set(prefix.add("displayName"), entity.getDisplayName());
    lookup.set(prefix.add("health"), entity.getHealth());
    lookup.set(prefix.add("foodLevel"), entity.getFoodLevel());
    addPlayer(lookup, (OfflinePlayer) entity, prefix, nullValue);
  }

  public static void addStats(
      @NonNull LazyVariableLookup lookup,
      @NonNull PlayerStatsAccumulator stats,
      @NonNull ArrayPath prefix) {
    for (PlayerStatsKey key : PlayerStatsKey.values())
      lookup.set(prefix.add(key.name().toLowerCase()), stats.findGet(key));
    lookup.set(prefix.add("lost"), stats.getMatchesLost());

    final double winChance = stats.getWinChance();
    lookup.set(prefix.add("winChance"), Suppliers.memoize(() -> {
      // winChance is defined outside for memoization reasons
      return String.format(Locale.ENGLISH, "%.1f", winChance) + '%';
    }));
    final double killDeathRatio = stats.getKillDeathRatio();
    lookup.set(prefix.add("kd"), Suppliers.memoize(() -> {
      // kills/deaths ratio is defined outside for memoization reasons
      return String.format(Locale.ENGLISH, "%.2f", killDeathRatio);
    }));
  }

  public static void addPlayer(
      @NonNull LazyVariableLookup lookup,
      @NonNull Player entity,
      @NonNull ArrayPath prefix) {
    addPlayer(lookup, entity, prefix, null);
  }

  public static void addTeam(
      @NonNull LazyVariableLookup lookup,
      @Nullable GameTeam team,
      @NonNull ArrayPath prefix,
      @Nullable Object nullValue) {
    lookup.set(prefix.add("displayName"), team != null
        ? team.getTeamEnum().getChatColor() + team.getTeamEnum().getTranslatedName()
        : nullValue);
    lookup.set(prefix.add("name"), team != null
        ? team.getTeamEnum().getTranslatedName()
        : nullValue);
    lookup.set(prefix.add("color"), team != null
        ? team.getTeamEnum().getChatColor()
        : nullValue);
  }

  public static void addTeam(
      @NonNull LazyVariableLookup lookup,
      @Nullable GameTeam team,
      @NonNull ArrayPath prefix) {
    addTeam(lookup, team, prefix, null);
  }

  public static void addKit(
      @NonNull LazyVariableLookup lookup,
      @Nullable SkywarsKit kit,
      @NonNull ArrayPath prefix,
      @Nullable Object nullValue) {
    lookup.set(prefix.add("name"), kit == null ? nullValue : kit.getName());
    lookup.set(prefix.add("displayName"), kit == null ? nullValue : kit.getDisplayName());
  }

  public static void addKit(
      @NonNull LazyVariableLookup lookup,
      @Nullable SkywarsKit kit,
      @NonNull ArrayPath prefix) {
    addKit(lookup, kit, prefix, null);
  }

  public static void addTicker(LazyVariableLookup lookup, Ticker ticker,
                               ArrayPath prefix) {
    for (TimeUnit unit : TimeUnit.values())
      lookup.set(prefix.add(unit.name()), ticker.getElapsed(unit));
    lookup.set(prefix.add("format"), Suppliers.memoize(() -> formatRelativeDate(ticker)));
    lookup.set(prefix.add("literal"), Suppliers.memoize(() -> formatLiteral(ticker)));
  }

  public static void addMatch(LazyVariableLookup lookup, SkywarsMatch match, ArrayPath prefix) {
    addMatch(lookup, match, prefix, null);
  }

  public static void addMatch(
      LazyVariableLookup lookup, SkywarsMatch match, ArrayPath prefix, Object nullValue) {
    lookup.set(prefix.add("id"), match.getId());
    lookup.set(prefix.add("arena"), match.getArena().getName());
    lookup.set(prefix.add("minPlayers"), match.getMinPlayerCount());
    lookup.set(prefix.add("maxPlayers"), match.getMaxPlayerCount());
    lookup.set(prefix.add("missing"), match.getMinPlayerCount() - match.getAudience().size());
    lookup.set(prefix.add("players"), match.getAudience().size());
    lookup.set(prefix.add("alive"), Suppliers.memoize(() -> match.getAudience().alive().count()));
    lookup.set(prefix.add("dead"), Suppliers.memoize(() -> match.getAudience().dead().count()));
    addState(lookup, match.getState(), prefix.add("state"));
    addTeam(lookup, match.getWinner(), prefix.add("winner"), nullValue);
    match.getCycler().getPhase().ifPresent((phase) -> {
      addTicker(lookup, phase.getTicker(), prefix.add(ArrayPath.of("time", "elapsed")));
      // create a new dummy ticker to invert the phase's ticker to represent time left
      TimeTicker left = new TimeTicker(TimeUnit.TICKS);
      left.set(phase.getDuration().toTicks() - phase.getTicker().getElapsed(TimeUnit.TICKS));
      addTicker(lookup, left, prefix.add(ArrayPath.of("time", "left")));
    });
  }

  /** Adds {@code arena} (or the match acquiring it) to {@code lookup} at offset {@code prefix}. */
  public static void addArenaOrAcquiree(
      LazyVariableLookup lookup, SkywarsArena arena, ArrayPath prefix) {
    SkywarsMatchManager matches = Skywars.getInstance().getMatchManager();
    lookup.setIfAbsent(prefix.add("name"), arena.getName());
    matches.find(arena).ifPresentOrElse((match) -> {
      addMatch(lookup, match, ArrayPath.of(), null);
    }, () -> {
      // Fill with values known when no match is associated to `arena`
      GameSettings settings = arena.getData().getSettings();
      int teamCount = 0; // approximate max team count (may change until match is created!)
      for (TeamEnum teamEnum : TeamEnum.values())
        if (arena.getData().getSpawns(teamEnum)
            .filter(Predicate.not(SpawnGroup::isEmpty))
            .isPresent())
          ++teamCount;
      int minPlayerCount = SkywarsArena.getMinPlayerCount(settings);
      addState(lookup, arena.isCompleted()
              ? SkywarsMatchState.IDLE
              : SkywarsMatchState.SETUP,
          ArrayPath.of("state"));
      lookup.set(ArrayPath.of("minPlayers"), minPlayerCount);
      lookup.set(ArrayPath.of("minPlayers"), minPlayerCount);
      lookup.set(ArrayPath.of("maxPlayers"), SkywarsArena.getMaxPlayerCount(settings, teamCount));
      lookup.set(ArrayPath.of("missing"), minPlayerCount);
      lookup.set(ArrayPath.of("players"), 0);
      lookup.set(ArrayPath.of("alive"), 0);
      lookup.set(ArrayPath.of("dead"), 0);
    });
  }

  public static void addState(
      LazyVariableLookup lookup, SkywarsMatchState state, ArrayPath prefix) {
    lookup.set(prefix.add("name"), state.getTranslatedName());
    lookup.set(prefix.add("color"), state.isJoinable() ? ChatColor.GREEN : ChatColor.RED);
  }

  public static String formatRelativeDate(Ticker ticker) {
    long seconds = ticker.getElapsed(TimeUnit.SECONDS);
    long minutes = ticker.getElapsed(TimeUnit.MINUTES);
    long hours = ticker.getElapsed(TimeUnit.HOURS);
    long days = ticker.getElapsed(TimeUnit.DAYS);
    boolean appendHours = hours >= 1 || days >= 1;
    boolean appendMinutes = minutes >= 1 || appendHours;
    boolean appendSeconds = seconds >= 1 || appendMinutes;
    StringBuilder builder = new StringBuilder();
    if (days >= 1)
      builder.append(StringUtils.leftPad(String.valueOf(days), 2, '0'));
    if (appendHours) {
      if (builder.length() != 0) builder.append(':');
      builder.append(StringUtils.leftPad(String.valueOf(hours % 24), 2, '0'));
    }
    if (appendMinutes) {
      if (builder.length() != 0) builder.append(':');
      builder.append(StringUtils.leftPad(String.valueOf(minutes % 60), 2, '0'));
    } else if (appendSeconds) {
      if (builder.length() != 0) builder.append(':');
      builder.append("00");
    }
    if (appendSeconds) {
      if (builder.length() != 0) builder.append(':');
      builder.append(StringUtils.leftPad(String.valueOf(seconds % 60), 2, '0'));
    }
    return builder.toString();
  }

  public static String formatLiteral(Ticker ticker) {
    long seconds = ticker.getElapsed(TimeUnit.SECONDS) % 60;
    long minutes = ticker.getElapsed(TimeUnit.MINUTES) % 60;
    long hours = ticker.getElapsed(TimeUnit.HOURS) % 24;
    long days = ticker.getElapsed(TimeUnit.DAYS);
    StringBuilder builder = new StringBuilder();
    Language language = Language.getInstance();
    if (days >= 1) {
      if (builder.length() != 0) builder.append(' ');
      builder.append(days).append(' ').append(language.get(
          MessageKeys.getTimeUnitKey(TimeUnit.SECONDS, days > 1)).get());
    }
    if (hours >= 1) {
      if (builder.length() != 0) builder.append(' ');
      builder.append(hours).append(' ').append(language.get(
          MessageKeys.getTimeUnitKey(TimeUnit.HOURS, hours > 1)).get());
    }
    if (minutes >= 1) {
      if (builder.length() != 0) builder.append(' ');
      builder.append(minutes).append(' ').append(language.get(
          MessageKeys.getTimeUnitKey(TimeUnit.MINUTES, minutes > 1)).get());
    }
    if (seconds >= 1) {
      if (builder.length() != 0) builder.append(' ');
      builder.append(seconds).append(' ').append(language.get(
          MessageKeys.getTimeUnitKey(TimeUnit.SECONDS, seconds > 1)).get());
    }
    return builder.toString();
  }

}
