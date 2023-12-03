package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.experimental.UtilityClass;
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

  private static final GameSettings DEFAULT_SETTINGS = of(2, Flags.PROTECTION_PHASE);

  private final int teamSize;

  private final int flags;

  private GameSettings(int teamSize, int flags) {
    Preconditions.checkArgument(teamSize >= 1, "Team size must be more than zero");
    Preconditions.checkArgument((flags & ~Flags.FLAGS) == 0, "Flags contain unknown flag(s)");
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

  @UtilityClass
  public static final class Flags {
    public static final int PROTECTION_PHASE = 1;

    private static final int FLAGS = PROTECTION_PHASE;

    public static boolean hasProtectionPhase(int flags) {
      return (flags & PROTECTION_PHASE) != 0;
    }
  }

}
