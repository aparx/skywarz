package io.github.aparx.skywarz.language;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.game.team.Team;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 09:42
 * @since 1.0
 */
public final class LocalizedMessage {

  public static final char VARIABLE_ESCAPE = '\\';
  public static final String VARIABLE_PREFIX = "{";
  public static final String VARIABLE_SUFFIX = "}";

  private final @NonNull String rawContent;

  private final @NonNull String content;

  private final StringSubstitutor defaultSubstitutor;

  public LocalizedMessage(@NonNull StringLookup defaultLookup, @NonNull String content) {
    Preconditions.checkNotNull(defaultLookup, "Lookup must not be null");
    Preconditions.checkNotNull(content, "Content must not be null");
    this.rawContent = content;
    this.content = ChatColor.translateAlternateColorCodes('&', rawContent);
    this.defaultSubstitutor = new StringSubstitutor(defaultLookup,
        VARIABLE_PREFIX, VARIABLE_SUFFIX, VARIABLE_ESCAPE);
  }

  public static boolean isEmpty(LocalizedMessage translatable) {
    return translatable == null || translatable.isEmpty();
  }

  public int length() {
    return rawContent.length();
  }

  public boolean isEmpty() {
    return rawContent.isEmpty();
  }

  public StringSubstitutor createSubstitutor() {
    return defaultSubstitutor;
  }

  public StringSubstitutor createSubstitutor(@Nullable Map<String, ?> data) {
    if (data != null && !data.isEmpty())
      return new StringSubstitutor((variable) -> {
        Object resolve = data.get(variable);
        if (resolve != null) return resolve.toString();
        resolve = defaultSubstitutor.getStringLookup().lookup(variable);
        if (resolve != null) return resolve.toString();
        return VARIABLE_PREFIX + variable + VARIABLE_SUFFIX;
      }, VARIABLE_PREFIX, VARIABLE_SUFFIX, VARIABLE_ESCAPE);
    return createSubstitutor();
  }

  @CheckReturnValue
  public String substitute(StringSubstitutor substitutor) {
    return substitutor.replace(get());
  }

  public String substitute(@Nullable Map<String, ?> valueMap) {
    return substitute(createSubstitutor(valueMap));
  }

  public String substitute() {
    return substitute(defaultSubstitutor);
  }

  public String substitute(Object @Nullable ... args) {
    if (ArrayUtils.isEmpty(args))
      return substitute();
    Map<String, Object> valueMap = new HashMap<>(args.length);
    for (int i = 0; i < args.length; ++i)
      valueMap.put(String.valueOf(i), args[i]);
    return substitute(valueMap);
  }

  public String substitute(@NonNull Player player, @NonNull ArrayPath prefix) {
    Map<String, Object> map = new HashMap<>();
    ValueMapPopulators.populatePlayer(map, player, prefix);
    return substitute(map);
  }

  public String substitute(@NonNull Team team, @NonNull ArrayPath prefix) {
    Map<String, Object> map = new HashMap<>();
    ValueMapPopulators.populateTeam(map, team, prefix);
    return substitute(map);
  }

  public @NonNull String getRawContent() {
    return rawContent;
  }

  public String get() {
    return content;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LocalizedMessage that = (LocalizedMessage) o;
    return Objects.equals(rawContent, that.rawContent)
        && Objects.equals(defaultSubstitutor, that.defaultSubstitutor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawContent, defaultSubstitutor);
  }

  @Override
  public String toString() {
    return "LocalizedMessage{" +
        "raw='" + rawContent + '\'' +
        ", content='" + content + '\'' +
        '}';
  }
}
