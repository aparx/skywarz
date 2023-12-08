package io.github.aparx.skywarz.language;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.kit.Kit;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.team.Team;
import io.github.aparx.skywarz.utils.tick.Ticker;
import io.github.aparx.skywarz.utils.tick.TimeTicker;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 14:33
 * @since 1.0
 */
@UtilityClass
public final class ValueMapPopulators {

  public static void populatePlayer(
      @NonNull LazyVariableLookup lookup,
      @NonNull Player entity,
      @NonNull ArrayPath prefix,
      @Nullable Object nullValue) {
    Preconditions.checkNotNull(entity, "Entity must not be null");
    lookup.set(prefix.add("name"), entity.getName());
    lookup.set(prefix.add("name"), entity.getDisplayName());
    lookup.set(prefix.add("health"), entity.getHealth());
    lookup.set(prefix.add("foodLevel"), entity.getFoodLevel());
    SkywarsPlayer.findPlayer(entity)
        .map(SkywarsPlayer::getPlayerData)
        .flatMap((storage) -> storage.find(PlayerMatchData.class))
        .ifPresent((matchData) -> {
          populateKit(lookup, matchData.getKit(), prefix.add("kit"), nullValue);
          populateTeam(lookup, matchData.getTeam(), prefix.add("team"), nullValue);
        });
  }

  public static void populatePlayer(
      @NonNull LazyVariableLookup lookup,
      @NonNull Player entity,
      @NonNull ArrayPath prefix) {
    populatePlayer(lookup, entity, prefix, null);
  }

  public static void populateTeam(
      @NonNull LazyVariableLookup lookup,
      @Nullable Team team,
      @NonNull ArrayPath prefix,
      @Nullable Object nullValue) {
    lookup.set(prefix.add("name"), team != null
        ? Language.getInstance().getTeamName(team.getTeamEnum())
        : nullValue);
    lookup.set(prefix.add("color"), team != null
        ? team.getTeamEnum().getChatColor()
        : nullValue);
  }

  public static void populateTeam(
      @NonNull LazyVariableLookup lookup,
      @Nullable Team team,
      @NonNull ArrayPath prefix) {
    populateTeam(lookup, team, prefix, null);
  }

  public static void populateKit(
      @NonNull LazyVariableLookup lookup,
      @Nullable Kit kit,
      @NonNull ArrayPath prefix,
      @Nullable Object nullValue) {
    lookup.set(prefix.add("name"), kit == null ? nullValue : kit.getName());
    lookup.set(prefix.add("displayName"), kit == null ? nullValue : kit.getDisplayName());
  }

  public static void populateKit(
      @NonNull LazyVariableLookup lookup,
      @Nullable Kit kit,
      @NonNull ArrayPath prefix) {
    populateKit(lookup, kit, prefix, null);
  }

  public static void populateTicker(LazyVariableLookup lookup, Ticker ticker,
                                    ArrayPath prefix) {
    for (TimeUnit unit : TimeUnit.values())
      lookup.set(prefix.add(unit.name().toLowerCase()), ticker.getElapsed(unit));
    lookup.set(prefix.add("format"), Suppliers.memoize(() -> formatRelativeDate(ticker)));
    lookup.set(prefix.add("literal"), Suppliers.memoize(() -> formatLiteral(ticker)));
  }

  public static void populateMatch(LazyVariableLookup lookup, Match match, ArrayPath prefix) {
    lookup.set(prefix.add("id"), match.getId());
    lookup.set(prefix.add("arena"), match.getArena().getName());
    lookup.set(prefix.add("minPlayers"), match.getMinPlayerSize());
    lookup.set(prefix.add("maxPlayers"), match.getMaxPlayerSize());
    lookup.set(prefix.add("missing"), match.getMinPlayerSize() - match.getAudience().size());
    lookup.set(prefix.add("players"), match.getAudience().size());
    lookup.set(prefix.add("alive"), Suppliers.memoize(() -> match.getAudience().alive().count()));
    lookup.set(prefix.add("dead"), Suppliers.memoize(() -> match.getAudience().dead().count()));
    match.getCycler().getPhase().ifPresent((phase) -> {
      populateTicker(lookup, phase.getTicker(), prefix.add(ArrayPath.of("time", "elapsed")));
      // create a new dummy ticker to invert the phase's ticker to represent time left
      TimeTicker left = new TimeTicker(TimeUnit.TICKS);
      left.set(phase.getDuration().toTicks() - phase.getTicker().getElapsed(TimeUnit.TICKS));
      populateTicker(lookup, left, prefix.add(ArrayPath.of("time", "left")));
    });
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
