package io.github.aparx.skywarz.utils.tick;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.utils.ConversionUtils;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 02:03
 * @since 1.0
 */
@Getter
@SerializableAs("Skywarz.TickDuration")
public final class TickDuration implements ConfigurationSerializable {

  private static final TickDuration ZERO_DURATION = new TickDuration(TimeUnit.TICKS, 0);

  private static final TickDuration SINGLE_TICK_DURATION = new TickDuration(TimeUnit.TICKS, 1);

  private static final TickDuration SINGLE_SECOND_DURATION = new TickDuration(TimeUnit.SECONDS, 1);

  private static final TickDuration NEVER_DURATION = new TickDuration(TimeUnit.HOURS,
      Integer.MAX_VALUE);

  private final @NonNull TimeUnit unit;
  private final long amount;

  private TickDuration(@NonNull TimeUnit unit, long amount) {
    Preconditions.checkNotNull(unit, "Unit must not be null");
    this.unit = unit;
    this.amount = amount;
  }

  public static TickDuration of() {
    return ZERO_DURATION;
  }

  public static TickDuration ofTick() {
    return SINGLE_TICK_DURATION;
  }

  public static TickDuration ofSecond() {
    return SINGLE_SECOND_DURATION;
  }

  public static TickDuration eternity() {
    return NEVER_DURATION;
  }

  public static TickDuration of(@NonNull TimeUnit unit, long amount) {
    if (amount == 0) return ZERO_DURATION;
    final long finalAmount = unit.toTicks(amount);
    if (finalAmount == 1)
      return SINGLE_TICK_DURATION;
    if (finalAmount == TimeUnit.SECONDS.toTicks(1))
      return SINGLE_SECOND_DURATION;
    return new TickDuration(unit, amount);
  }

  public static TickDuration deserialize(Map<?, ?> args) {
    return new TickDuration(
        ConversionUtils.toEnum(args.get("unit"), TimeUnit.class),
        NumberConversions.toInt(args.get("amount")));
  }

  @Override
  public @NonNull Map<String, Object> serialize() {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    // we ensure the alias is present, even for config fields (for future reference)
    // TODO this isn't necessarily needed, but kept in for future changes for now
    map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
        ConfigurationSerialization.getAlias(TickDuration.class));
    map.put("unit", unit.name().toLowerCase());
    map.put("amount", amount);
    return map;
  }

  public TickDuration add(long amount) {
    return new TickDuration(getUnit(), getAmount() + amount);
  }

  public TickDuration add(TickDuration duration) {
    return add(duration.getAmount(getUnit()));
  }

  public TickDuration multiply(long amount) {
    return new TickDuration(getUnit(), getAmount() * amount);
  }

  public TickDuration convertTo(@NonNull TimeUnit target) {
    Preconditions.checkNotNull(target, "Target unit must not be null");
    if (target == unit) return this;
    return of(target, getAmount(target));
  }

  public long getAmount(@NonNull TimeUnit target) {
    Preconditions.checkNotNull(target, "Target unit must not be null");
    if (target == unit) return amount;
    else if (target == TimeUnit.TICKS)
      return amount * unit.toTicks();
    long targetAsTicks = target.toTicks();
    long thisAsTicks = unit.toTicks();
    long tickDiff = targetAsTicks - thisAsTicks;
    /*return tickDiff == 0 ? amount : (tickDiff > 0
        ? (amount / targetAsTicks) * thisAsTicks
        : amount * (thisAsTicks / targetAsTicks));*/
    return (tickDiff == 0 ? amount : (tickDiff > 0
        ? (amount * thisAsTicks) / targetAsTicks
        : amount * (thisAsTicks / targetAsTicks)));
  }

  public long toTicks() {
    return getAmount(TimeUnit.TICKS);
  }

  public TickDuration asTicks() {
    return convertTo(TimeUnit.TICKS);
  }

  public long toSeconds() {
    return getAmount(TimeUnit.SECONDS);
  }

  public TickDuration asSeconds() {
    return convertTo(TimeUnit.SECONDS);
  }

  public long toMinutes() {
    return getAmount(TimeUnit.MINUTES);
  }

  public TickDuration asMinutes() {
    return convertTo(TimeUnit.MINUTES);
  }

  public long toHours() {
    return getAmount(TimeUnit.HOURS);
  }

  public TickDuration asHours() {
    return convertTo(TimeUnit.HOURS);
  }

  public long toDays() {
    return getAmount(TimeUnit.DAYS);
  }

  public TickDuration asDays() {
    return convertTo(TimeUnit.DAYS);
  }

  @Override
  public String toString() {
    return "TickDuration{" +
        "unit=" + unit +
        ", amount=" + amount +
        '}';
  }


}
