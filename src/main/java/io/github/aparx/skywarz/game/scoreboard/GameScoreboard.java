package io.github.aparx.skywarz.game.scoreboard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.LazyVariableLookup;
import io.github.aparx.skywarz.language.ValueMapPopulators;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-08 11:46
 * @since 1.0
 */
@Getter
public class GameScoreboard extends ConfigObject {

  private final @NonNull MatchState state;

  private final @NonNull TickDuration updateInterval;

  private final @Nullable String name;

  @ConfigMapping("title")
  private String title;

  @ConfigMapping("lines")
  @Document({
      "The different lines of this scoreboard.",
      "Tip: use less than 15 characters for better readability."
  })
  private @NonNull List<String> templateLines;

  public GameScoreboard(
      @NonNull MatchState state,
      @Nullable String name,
      @NonNull TickDuration intervalUpdate,
      @Nullable String initialTitle) {
    this(state, name, intervalUpdate, initialTitle, new ArrayList<>());
  }

  public GameScoreboard(
      @NonNull MatchState state,
      @NonNull TickDuration updateInterval,
      @Nullable String initialTitle,
      @NonNull List<String> initialTemplateLines) {
    this(state, null, updateInterval, initialTitle, initialTemplateLines);
  }

  public GameScoreboard(
      @NonNull MatchState state,
      @Nullable String name,
      @NonNull TickDuration updateInterval,
      @Nullable String initialTitle,
      @NonNull List<String> initialTemplateLines) {
    super((proxy) -> Skywars.getInstance().getConfigHandler().getOrCreate("scoreboards"));
    Preconditions.checkNotNull(state, "State must not be null");
    Preconditions.checkNotNull(updateInterval, "Interval must not be null");
    Preconditions.checkNotNull(initialTemplateLines, "Lines must not be null");
    this.state = state;
    this.name = name;
    this.title = initialTitle;
    this.updateInterval = updateInterval;
    this.templateLines = initialTemplateLines;
  }

  public void setTemplateLines(@NonNull List<String> templateLines) {
    Preconditions.checkNotNull(templateLines, "Lines must not be null");
    this.templateLines = templateLines;
  }

  public SpecialScoreboard createScoreboard(@NonNull Match match, @Nullable Player holder) {
    return new SpecialScoreboard(updateInterval, (sb) -> createContent(sb, match, holder));
  }

  @CheckReturnValue
  protected ScoreboardContent createContent(
      @NonNull SpecialScoreboard scoreboard, @NonNull Match match, @Nullable Player holder) {
    LazyVariableLookup lookup = new LazyVariableLookup();
      ValueMapPopulators.populateMatch(lookup, match, ArrayPath.of("match"));
    if (holder != null)
      ValueMapPopulators.populatePlayer(lookup, holder, ArrayPath.of("player"), "-");
    String[] array = new String[templateLines.size()];
    for (int i = 0; i < array.length; ++i)
      array[i] = Language.getInstance().substitute(templateLines.get(i), lookup);
    return ScoreboardContent.builder()
        .title(Language.getInstance().substitute(getTitle(), lookup))
        .lines(array)
        .build();
  }

  @Override
  public @NonNull ArrayPath getOffsetPath() {
    return ArrayPath.of(getState().name().toLowerCase(), getName());
  }
}
