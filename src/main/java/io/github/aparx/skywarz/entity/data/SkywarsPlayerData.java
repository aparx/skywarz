package io.github.aparx.skywarz.entity.data;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.RegisterNotifiable;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 05:03
 * @since 1.0
 */
public abstract class SkywarsPlayerData implements RegisterNotifiable {

  private static final Map<Class<?>, Constructor<?>> constructors = new ConcurrentHashMap<>();

  public static <T extends SkywarsPlayerData> T newInstance(
      @NonNull Class<T> type, @NonNull SkywarsPlayer player) {
    Preconditions.checkNotNull(type, "Type must not be null");
    Preconditions.checkNotNull(player, "Player (owner) must not be null");
    try {
      Constructor<T> constructor = getConstructor(type);
      if (constructor.getParameterCount() != 0)
        return constructor.newInstance(player);
      return constructor.newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException("Could not create new data instance", e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends SkywarsPlayerData> Constructor<T> getConstructor(@NonNull Class<T> type) {
    Preconditions.checkNotNull(type, "Type must not be null");
    return (Constructor<T>) constructors.computeIfAbsent(type, SkywarsPlayerData::getConstructor0);
  }

  private static Constructor<?> getConstructor0(Class<?> type) {
    try {
      return type.getConstructor(SkywarsPlayer.class);
    } catch (NoSuchMethodException ignored) {}
    try {
      return type.getConstructor();
    } catch (NoSuchMethodException ex) {
      throw new RuntimeException("Missing default constructor", ex);
    }
  }

  /** Called when the underlying player has been registered (joined) */
  public void notifyRegister() {}

  /** Called when the underlying player has been removed (quit) */
  public void notifyRemoval() {}

}
