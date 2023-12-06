package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 06:35
 * @since 1.0
 */
@Getter
@SerializableAs("Skywarz.GameSettings")
public final class GameSettings implements ConfigurationSerializable {

  private static final GameSettings DEFAULT_SETTINGS = of(1, Flag.of(
      Flag.PROTECTION_PHASE, Flag.CHEST_RESET));

  @With
  private final int teamSize;

  @With
  private final int flags;

  private GameSettings(int teamSize, int flags) {
    Preconditions.checkArgument(teamSize >= 1, "Team size must be more than zero");
    Preconditions.checkArgument((flags & ~Flag.FLAGS) == 0, "Flags contain unknown flag(s)");
    this.teamSize = teamSize;
    this.flags = flags;
  }

  public static GameSettings of(@NonNegative int teamSize, int flags) {
    return new GameSettings(teamSize, flags);
  }

  public static GameSettings of() {
    return DEFAULT_SETTINGS;
  }

  public static GameSettings deserialize(@NonNull Map<?, ?> data) {
    return new GameSettings(NumberConversions.toInt(data.get("teamSize")),
        NumberConversions.toInt(data.get("flags")));
  }

  @Override
  public Map<String, Object> serialize() {
    return Map.of("teamSize", teamSize, "flags", getFlags());
  }

  @Getter
  @RequiredArgsConstructor
  public enum Flag {
    PROTECTION_PHASE(1),
    CHEST_RESET(2);

    private static final int FLAGS = of(values());

    private final int mask;

    public boolean isFlagged(int flags) {
      return (flags & getMask()) != 0;
    }

    public static int of(Flag... flags) {
      if (ArrayUtils.isEmpty(flags))
        return 0;
      int accumulate = 0;
      for (Flag flag : values())
        accumulate |= flag.mask;
      return accumulate;
    }
  }

}
