package io.github.aparx.skywarz.handler;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.Config;
import io.github.aparx.bufig.defaults.yaml.YamlConfig;
import io.github.aparx.bufig.handler.ConfigMap;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 02:34
 * @since 1.0
 */
@Getter
public final class SkywarsConfigHandler extends ConfigMap<Config> {

  public static final String ARENA_CONFIG_ID = ".storage/arenas";

  public SkywarsConfigHandler(@NonNull Plugin plugin) {
    super((id) -> new YamlConfig(id, new File(plugin.getDataFolder(), id + ".yml")));
  }

  public static String[] createHeader(@NonNull String @NonNull ... content) {
    return createHeader(content, 3 /* DEFAULT_HEADER_PADDING */);
  }

  public static String[] createHeader(@NonNull String @NonNull [] content, int padding) {
    Preconditions.checkNotNull(content, "Header content must not be null");
    Preconditions.checkArgument(padding >= 0, "Padding must be positive");
    int maxLength = 0;
    for (String line : content)
      maxLength = Math.max(maxLength, line.length());
    if (maxLength == 0) return null;
    String[] newArray = new String[2 + content.length];
    for (int i = 0; i < content.length; ++i) {
      String line = content[i];
      newArray[1 + i] = " ".repeat(padding + ((maxLength - line.length()) / 2)) + line;
    }
    newArray[0] = '<' + "=".repeat(2 * Math.max((padding - 1), 0) + maxLength) + '>';
    newArray[newArray.length - 1] = newArray[0];
    return newArray;
  }

  public @NonNull Config getArenas() {
    return getOrCreate(ARENA_CONFIG_ID);
  }

}
