package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.arena.ArenaBox;
import io.github.aparx.skywarz.game.arena.GameSettings;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 08:59
 * @since 1.0
 */
public interface IArenaData {

  @NonNull ArenaBox getBox();

  Location getSpectator();

  Location getLobby();

  @NonNull World getWorld();

  @NonNull Map<String, SpawnGroup> getSpawns();

  @NonNull GameSettings getSettings();

  static @NonNull World getWorldFromReference(WeakReference<World> reference) {
    Preconditions.checkNotNull(reference, "World reference is null");
    World world = reference.get();
    Preconditions.checkState(world != null, "World has become invalid");
    return world;
  }

}
