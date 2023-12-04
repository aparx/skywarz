package io.github.aparx.skywarz.entity;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.utils.collection.WeakHashSet;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 04:34
 * @since 1.0
 */
public class WeakGroupAudience<T extends Audience> extends WeakHashSet<T>
    implements Iterable<T>, Audience {

  public WeakGroupAudience() {}

  public WeakGroupAudience(Collection<? extends T> initialMembers) {
    super(initialMembers);
  }

  @Override
  public void playSound(Location location, Sound sound, float volume, float pitch) {
    forEach((member) -> member.playSound(location, sound, volume, pitch));
  }

  @Override
  public void playSound(Sound sound, float volume, float pitch) {
    forEach((member) -> member.playSound(sound, volume, pitch));
  }

  @Override
  public void playTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    forEach((member) -> member.playTitle(title, subtitle, fadeIn, stay, fadeOut));
  }

  @Override
  public void playActionbar(String message) {
    forEach((member) -> member.playActionbar(message));
  }

  @Override
  public void sendMessage(Object message) {
    forEach((member) -> member.sendMessage(message));
  }

  @Override
  public @NonNull Iterator<T> iterator() {
    // avoid concurrency issues by copying for iterations
    return Set.copyOf(internalMap.keySet()).iterator();
  }
}
