package io.github.aparx.skywarz.handler;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.match.SkywarsMatchState;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 03:37
 * @since 1.0
 */
@Getter
public class MainConfig extends ConfigObject {

  @Getter
  public static final MainConfig instance = new MainConfig();

  @ConfigMapping("bungeecord.enabled")
  @Document({
      "If true, this server is seen as a dedicated Skywarz server.",
      "This implies that only one arena is active at once for this server."
  })
  private boolean isDedicated = false;

  @ConfigMapping("bungeecord.arena")
  @Document({
      "The dedicated arena that is represented by this dedicated server.",
      "If the above boolean is true, a player will be put into a match for this arena.",
      "If the arena does not exist and the player has no setup permission, they are kicked."
  })
  private String dedicatedArena = "<ArenaName>";

  @ConfigMapping("bungeecord.motd")
  @Document("The Motd when the Bungeecord mode is enabled (can be used for external analysis)")
  private List<String> dedicatedMotd = List.of(
      "{name}",
      "{state.color}{state.name}"
  );

  @ConfigMapping("celebration.enabled")
  @Document({
      "If enabled, effects will spawn to celebrate a winner at the lobby when a match is done"
  })
  private boolean celebrationEnabled = true;

  @ConfigMapping("celebration.firework spawn radius")
  @Document("The radius in which fireworks randomly spawn to celebrate the winner")
  private double celebrationFireworkRadius = 20.0;

  @ConfigMapping("celebration.firework height")
  @Document("The minimum height offset to the lobby spawn at which fireworks spawn")
  private double celebrationFireworkHeight = 5;

  @ConfigMapping("chat.everyone tag")
  @Document({
      "When a player is not alone in a team, they will automatically only be able to chat",
      "with their team, unless they use one of following tags at the beginning of their message.",
      "The tags are case-insensitive."
  })
  private List<String> everyoneChatTargetTags = List.of(
      "@everyone",
      "@all",
      "@a"
  );

  @Getter(AccessLevel.NONE)
  @ConfigMapping("duration.phases")
  @Document("The different durations of all running game phases")
  // Note: we use a Map with string keys over an EnumMap due to serialization
  private Map<String, TickDuration> phaseDurationMap = new HashMap<>();

  @ConfigMapping("duration.protection")
  @Document("How long players cannot take damage (protection time)")
  private TickDuration protectionDuration = TickDuration.of(TimeUnit.SECONDS, 30);

  private MainConfig() {
    super((proxy) -> Skywars.getInstance().getConfigHandler().getOrCreate("main"));
    Arrays.stream(SkywarsMatchState.values()).forEach((unit) -> {
      TickDuration defaultDuration = unit.getDefaultDuration();
      if (defaultDuration != null)
        setPhaseDuration(unit, defaultDuration);
    });
  }

  @Override
  public void save() {
    setHeader(SkywarsConfigHandler.createHeader(
        "The main configuration of Skywarz (by @bonedfps)",
        "Here contained are general purpose settings"
    ));
    setDocsIfAbsent("duration", "Valid units: 'ticks', 'seconds', 'minutes', 'hours' and 'days'");
    super.save();
  }

  public void setPhaseDuration(@NonNull SkywarsMatchState state, @NonNull TickDuration duration) {
    Preconditions.checkNotNull(state, "State must not be null");
    Preconditions.checkNotNull(duration, "Duration must not be null");
    phaseDurationMap.put(state.name(), duration);
  }

  public TickDuration getPhaseDuration(@NonNull SkywarsMatchState state) {
    return phaseDurationMap.get(state.name());
  }

}
