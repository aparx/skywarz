package io.github.aparx.skywarz.entity;

import io.github.aparx.skywarz.handler.configs.Language;
import org.bukkit.Location;
import org.bukkit.Sound;

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

  default void sendMessage(Function<Language, Object> message) {
    // this method may be overridden if a language is dependent on a player
    sendMessage(message.apply(Language.getLanguage()));
  }

  default void sendMessage(String message, Object... args) {
    if (args != null) sendMessage(String.format(message, args));
    else sendMessage(message);
  }

}
