package io.github.aparx.skywarz.game.arena.settings;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.bufig.utils.ConversionUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.WeatherType;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 06:35
 * @since 1.0
 */
@SerializableAs("Skywarz.GameSettings")
public final class ArenaSettings implements ConfigurationSerializable {

  private static final ArenaSettings DEFAULT_SETTINGS = ArenaSettings.builder().build();

  private final EnumMap<ArenaRule, Object> ruleMap;

  private ArenaSettings(EnumMap<ArenaRule, Object> delegateRuleMap) {
    Preconditions.checkNotNull(delegateRuleMap, "Rule Map must not be null");
    this.ruleMap = delegateRuleMap;
  }

  public static ArenaSettingsBuilder builder() {
    return new ArenaSettingsBuilder(new EnumMap<>(ArenaRule.class));
  }

  public static ArenaSettings copyOf(@NonNull EnumMap<ArenaRule, Object> ruleMap) {
    Preconditions.checkNotNull(ruleMap, "Rule Map must not be null");
    return new ArenaSettings(ruleMap.clone());
  }

  public static ArenaSettings of() {
    return DEFAULT_SETTINGS;
  }

  public static ArenaSettings deserialize(@NonNull Map<?, ?> data) {
    return DEFAULT_SETTINGS.withRules(ConversionUtils.toEnumMap(
        ConversionUtils.toGenericStringMap(data.get("rules")),
        ArenaRule.class));
  }

  @Override
  public @NonNull Map<String, Object> serialize() {
    Map<String, Object> map = ConversionUtils.toGenericStringMap(ruleMap);
    for (Map.Entry<String, Object> entry : map.entrySet())
      if (entry.getValue() instanceof Enum<?>)
        entry.setValue(((Enum<?>) entry.getValue()).name());
    return Map.of("rules", map);
  }

  public @Nullable Object getRuleValue(@NonNull ArenaRule rule) {
    Object object = ruleMap.get(rule);
    if (object == null)
      return rule.getRule().getDefaultValue();
    return object;
  }

  public Optional<Object> getRuleValueOptional(@NonNull ArenaRule rule) {
    return Optional.ofNullable(getRuleValue(rule));
  }

  public int getTeamSize() {
    return (int) getRuleValueOptional(ArenaRule.TEAM_SIZE).orElse(1);
  }

  public @Nullable WeatherType getWorldWeather() {
    return (WeatherType) getRuleValue(ArenaRule.WORLD_WEATHER);
  }

  public @Nullable Integer getWorldTime() {
    return (Integer) getRuleValue(ArenaRule.WORLD_TIME);
  }

  public boolean getChestRefill() {
    return (boolean) getRuleValueOptional(ArenaRule.CHEST_REFILL).orElse(false);
  }

  public boolean getProtectionPhase() {
    return (boolean) getRuleValueOptional(ArenaRule.PROTECTION_PHASE).orElse(false);
  }

  @CheckReturnValue
  public ArenaSettings withRule(@NonNull ArenaRule rule, Object value) {
    Preconditions.checkNotNull(rule, "Rule must not be null");
    ArenaSettingsBuilder builder = new ArenaSettingsBuilder(ruleMap.clone());
    builder.set(rule, value);
    return builder.delegatedBuild();
  }

  @CheckReturnValue
  public ArenaSettings withRules(@NonNull Map<ArenaRule, Object> ruleMap) {
    Preconditions.checkNotNull(ruleMap, "Map must not be null");
    ArenaSettingsBuilder builder = new ArenaSettingsBuilder(this.ruleMap.clone());
    for (Map.Entry<ArenaRule, Object> entry : ruleMap.entrySet())
      builder.set(entry.getKey(), entry.getValue());
    return builder.delegatedBuild();
  }

  @RequiredArgsConstructor
  public static class ArenaSettingsBuilder {
    private final EnumMap<ArenaRule, Object> ruleMap;

    @CanIgnoreReturnValue
    public ArenaSettingsBuilder set(ArenaRule rule, Object value) {
      ruleMap.put(rule, rule.getRule().validate(value));
      return this;
    }

    public @Nullable Object get(ArenaRule rule) {
      return ruleMap.get(rule);
    }

    @CheckReturnValue
    public ArenaSettings build() {
      return copyOf(ruleMap);
    }

    protected ArenaSettings delegatedBuild() {
      return new ArenaSettings(ruleMap);
    }
  }


}