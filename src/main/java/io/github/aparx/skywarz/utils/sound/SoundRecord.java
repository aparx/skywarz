package io.github.aparx.skywarz.utils.sound;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.entity.Audience;
import lombok.Getter;
import lombok.With;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-07 09:46
 * @since 1.0
 */
@With
@Getter
public final class SoundRecord {

  public static final SoundRecord ACTION_SUCCESS = of(Sound.ENTITY_ITEM_PICKUP);
  public static final SoundRecord ACTION_ERROR = of(Sound.BLOCK_ANVIL_LAND, .33f, .75f);
  public static final SoundRecord OPEN_INVENTORY = of(Sound.BLOCK_LEVER_CLICK);
  public static final SoundRecord TIMER_TICK = of(Sound.BLOCK_DISPENSER_DISPENSE, .5f, 1.5f);

  private final @NonNull Sound sound;
  private final float volume;
  private final float pitch;

  private SoundRecord(@NonNull Sound sound, float volume, float pitch) {
    Preconditions.checkNotNull(sound, "Sound must not be null");
    Preconditions.checkState(volume >= 0 && volume <= 1, "Volume must be between 0 and 1");
    Preconditions.checkState(pitch >= 0 && pitch <= 1, "Volume must be between 0 and 1");
    this.sound = sound;
    this.volume = volume;
    this.pitch = pitch;
  }

  public static SoundRecord of(@NonNull Sound sound, float volume, float pitch) {
    volume = Math.min(Math.max(volume, 0.0F), 1.0F);
    pitch = Math.min(Math.max(pitch, 0.0F), 1.0F);
    return new SoundRecord(sound, volume, pitch);
  }

  public static SoundRecord of(@NonNull Sound sound, float volume) {
    return of(sound, volume, 1.0F);
  }

  public static SoundRecord of(@NonNull Sound sound) {
    return of(sound, 1.0F, 1.0F);
  }

  public void play(@NonNull Audience audience, Location location) {
    audience.playSound(location, sound, volume, pitch);
  }

  public void play(@NonNull Audience audience) {
    audience.playSound(sound, volume, pitch);
  }

  public void play(@NonNull Player entity, Location location) {
    entity.playSound(location, sound, volume, pitch);
  }

  public void play(@NonNull Player entity) {
    entity.playSound(entity.getLocation(), sound, volume, pitch);
  }

}
