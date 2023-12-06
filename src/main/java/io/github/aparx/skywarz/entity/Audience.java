package io.github.aparx.skywarz.entity;

import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.language.Language;
import org.bukkit.Location;
import org.bukkit.Sound;

import java.util.Map;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 04:41
 * @since 1.0
 */
public interface Audience {

  void playSound(Location location, Sound sound, float volume, float pitch);

  void playSound(Sound sound, float volume, float pitch);

  void playTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut);

  void playActionbar(String message);

  void sendMessage(Object message);

  default void sendMessage(String message, Object... args) {
    if (args != null) sendMessage(String.format(message, args));
    else sendMessage(message);
  }

  default void sendFormattedMessage(ArrayPath messagePath) {
    sendMessage(Language.getInstance().get(messagePath).substitute());
  }

  default void sendFormattedMessage(ArrayPath messagePath, Map<String, ?> valueMap) {
    sendMessage(Language.getInstance().get(messagePath).substitute(valueMap), valueMap);
  }

}
