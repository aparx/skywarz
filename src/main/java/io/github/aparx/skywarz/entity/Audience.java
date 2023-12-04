package io.github.aparx.skywarz.entity;

import io.github.aparx.skywarz.handler.configs.Language;
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

  default void playActionbar(Function<Language, String> message) {
    // this method may be overridden if a language is dependent on a player
    playActionbar(message.apply(Language.getLanguage()));
  }

  void sendMessage(Object message);

  default void sendMessage(Function<Language, Object> message) {
    // this method may be overridden if a language is dependent on a player
    Language language = Language.getLanguage();
    sendMessage(language.substitute(String.valueOf(message.apply(language))));
  }

  default void sendMessage(Function<Language, String> message, Map<String, ?> valueMap) {
    // this method may be overridden if a language is dependent on a player
    Language language = Language.getLanguage();
    sendMessage(language.substitute(message.apply(language), valueMap));
  }

  default void sendMessage(String message, Object... args) {
    if (args != null) sendMessage(String.format(message, args));
    else sendMessage(message);
  }

}
