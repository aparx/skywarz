package io.github.aparx.skywarz.entity;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 04:34
 * @since 1.0
 */
public class WeakGroupAudience<T extends Audience> extends AbstractSet<T>
    implements Iterable<T>, Audience {

  protected static final int DEFAULT_CAPACITY = 10;

  private static final Object VALUE = new Object();

  private final @NonNull WeakHashMap<T, Object> weakMap;

  public WeakGroupAudience() {
    this(DEFAULT_CAPACITY);
  }

  public WeakGroupAudience(int initialCapacity) {
    this.weakMap = new WeakHashMap<>(initialCapacity);
  }

  public WeakGroupAudience(Collection<T> initialMembers) {
    this(initialMembers.size());
    if (!initialMembers.isEmpty())
      addAll(initialMembers);
  }

  @Override
  public int size() {
    return weakMap.size();
  }

  @CanIgnoreReturnValue
  public boolean add(T member) {
    return weakMap.putIfAbsent(member, VALUE) != VALUE;
  }

  @CanIgnoreReturnValue
  public boolean remove(Object member) {
    return weakMap.remove(member) == VALUE;
  }

  @CanIgnoreReturnValue
  @SuppressWarnings("SuspiciousMethodCalls")
  public boolean contains(Object member) {
    return weakMap.containsKey(member);
  }

  @Override
  public void clear() {
    weakMap.clear();
  }

  @Override
  public @NonNull Iterator<T> iterator() {
    return weakMap.keySet().iterator();
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    WeakGroupAudience<?> that = (WeakGroupAudience<?>) o;
    return Objects.equals(weakMap, that.weakMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), weakMap);
  }

}
