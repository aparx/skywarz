package io.github.aparx.skywarz.handler.configs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.bufig.handler.ConfigHandler;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.team.TeamEnum;
import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 03:24
 * @since 1.0
 */
@Getter
public final class Language extends ConfigObject {

  private static final Map<String, String> defaultTeamNames = new HashMap<>();

  static {
    Arrays.stream(TeamEnum.values()).forEach((constant) -> {
      defaultTeamNames.put(constant.name(), constant.getDefaultName());
    });
  }

  private static final String SUBSTITUTOR_PREFIX = "{";

  private static final String SUBSTITUTOR_SUFFIX = "}";

  private transient ImmutableMap<String, ?> defaultValueMap;

  private transient StringSubstitutor defaultSubstitutor;

  // MESSAGES BEGIN

  @ConfigMapping("prefix.default")
  private String prefix = ChatColor.AQUA + "[Skywarz]" + ChatColor.RESET;

  @ConfigMapping("prefix.warning")
  private String warningPrefix = ChatColor.RED + "[Skywarz]";

  @ConfigMapping("prefix.success")
  private String successPrefix = ChatColor.GREEN + "[Skywarz]";

  @ConfigMapping("log.loaded")
  private String loadedLog = "{successPrefix} Skywarz has been loaded";

  @ConfigMapping("log.unloaded")
  private String unloadedLog = "{successPrefix} Skywarz has been unloaded";

  // ERRORS BEGIN

  @ConfigMapping("errors.error")
  private String error = "{warningPrefix} Error: {message}";

  @ConfigMapping("errors.not a number")
  private String errorNotNumber = "{warningPrefix} Value {value} is not a number!";

  @ConfigMapping("errors.not an integer")
  private String errorNotInteger = "{warningPrefix} Value {value} is not an integer!";

  @ConfigMapping("errors.negative number")
  private String errorNotPositive = "{warningPrefix} Value {value} must be positive!";

  @ConfigMapping("errors.self not a player")
  private String errorSelfNotPlayer = "{warningPrefix} Be a player to perform this action!";

  @ConfigMapping("errors.command syntax")
  private String errorCommandSyntax = "{warningPrefix} Syntax error: {usage}";

  @ConfigMapping("errors.command not found")
  private String errorCommandNotFound = "{warningPrefix} Command not found, try §r/{label} help";

  @ConfigMapping("errors.missing permission")
  private String errorPermission = "{warningPrefix} Missing permission to perform this action!";

  @ConfigMapping("errors.in a match")
  private String errorInMatch = "{warningPrefix} You are already in a match!";

  @ConfigMapping("errors.not in a match")
  private String errorNotInMatch = "{warningPrefix} You are not in a match!";

  @ConfigMapping("errors.cannot join match")
  private String errorJoinMatch = "{warningPrefix} You cannot join this match.";

  @ConfigMapping("errors.arena not found")
  private String errorArenaNotFound = "{warningPrefix} Arena {0} not found!";

  // ERRORS END

  @ConfigMapping("team names")
  private Map<String, String> teamNames = new HashMap<>(defaultTeamNames);

  // SUCCESS START
  @ConfigMapping("success.join match")
  private String successJoinMatch = "{successPrefix} You joined the match!";

  @ConfigMapping("success.leave match")
  private String successLeaveMatch = "{successPrefix} You left the match!";
  // SUCCESS END

  // BROADCAST START
  @ConfigMapping("broadcast.joined match")
  private String broadcastJoinedMatch = "§b[+]§7 Player §r{player.name}§7 joined the game!";

  @ConfigMapping("broadcast.left match")
  private String broadcastLeftMatch = "§c[-]§7 Player §r{player.name}§7 left the game!";
  // BROADCAST END

  // MESSAGES END

  public Language(@NonNull String languageFile) {
    this(languageFile, Skywars.getInstance().getConfigHandler());
    reassignDefaults();
  }

  public Language(@NonNull String languageFile, @NonNull ConfigHandler<?> handler) {
    super(languageFile, handler);
    reassignDefaults();
  }

  public static @NonNull Language getLanguage() {
    return Skywars.getInstance().getConfigHandler().getLanguage();
  }

  public static StringSubstitutor createPlainSubstitutor(Map<String, ?> valueMap) {
    return new StringSubstitutor(valueMap, SUBSTITUTOR_PREFIX, SUBSTITUTOR_SUFFIX);
  }

  public static Map<String, Object> newValueMapFromPlayer(Player player, String prefix) {
    HashMap<String, Object> map = new HashMap<>();
    map.put(prefix + ".name", player.getName());
    map.put(prefix + ".displayName", player.getDisplayName());
    map.put(prefix + ".health", String.format("%.1f", player.getHealth() / 2D));
    map.put(prefix + ".foodLevel", String.format("%.1f", player.getFoodLevel() / 2D));
    return map;
  }

  public static Map<String, Object> toValueMap(Object @NonNull [] array) {
    Preconditions.checkNotNull(array, "Array must not be null");
    Map<String, Object> valueMap = new HashMap<>(array.length);
    for (int i = 0; i < array.length; ++i)
      valueMap.put(String.valueOf(i), array[i]);
    return valueMap;
  }

  @Override
  public void save() {
    super.save();
    reassignDefaults();
  }

  public String substitute(@NonNull Function<Language, String> fn) {
    return substitute(fn.apply(this));
  }

  public String substitute(@NonNull Function<Language, String> fn, Map<String, ?> valueMap) {
    return substitute(fn.apply(this), valueMap);
  }

  public String substitute(@NonNull Function<Language, String> fn, Object... values) {
    return substitute(fn.apply(this), values);
  }

  public String substitute(@NonNull CharSequence message) {
    return getDefaultSubstitutor().replace(message);
  }

  public String substitute(@NonNull CharSequence message, Map<String, ?> valueMap) {
    return valueMap != null && !valueMap.isEmpty()
        ? createDefaultSubstitutor(valueMap).replace(message)
        : substitute(message);
  }

  public String substitute(@NonNull CharSequence message, Object... values) {
    return ArrayUtils.isNotEmpty(values)
        ? createDefaultSubstitutor(toValueMap(values)).replace(message)
        : substitute(message);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public StringSubstitutor createDefaultSubstitutor(Map<String, ?> valueMap) {
    Map<String, ?> newValueMap = new HashMap<>(defaultValueMap);
    newValueMap.putAll((Map) valueMap);
    return new StringSubstitutor(newValueMap, SUBSTITUTOR_PREFIX, SUBSTITUTOR_SUFFIX);
  }

  public Map<String, ?> newDefaultValueMap() {
    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("prefix", getPrefix());
    valueMap.put("warningPrefix", getWarningPrefix());
    valueMap.put("successPrefix", getSuccessPrefix());
    getHandle().getValues(this).forEach((field) -> {
      // reference a message defined in this language to `messages.<messageName>`
      valueMap.put("message." + field.getName(), field.get(this));
    });
    return valueMap;
  }

  public String getTeamName(TeamEnum team) {
    String mappedName = teamNames.get(team.name());
    if (StringUtils.isBlank(mappedName))
      return team.getDefaultName();
    return mappedName;
  }

  private void reassignDefaults() {
    defaultValueMap = ImmutableMap.copyOf(newDefaultValueMap());
    defaultSubstitutor = createDefaultSubstitutor(Map.of());
  }

}
