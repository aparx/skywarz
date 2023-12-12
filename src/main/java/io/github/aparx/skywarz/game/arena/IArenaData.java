package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.arena.ArenaBox;
import io.github.aparx.skywarz.game.arena.GameSettings;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.setup.CompletableSetup;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 08:59
 * @since 1.0
 */
public interface IArenaData extends CompletableSetup {

  @NonNull ArenaBox getBox();

  Location getSpectator();

  Location getLobby();

  @NonNull World getWorld();

  @NonNull EnumMap<TeamEnum, SpawnGroup> getSpawns();

  @NonNull GameSettings getSettings();

  @Override
  default boolean isCompleted() {
    return getBox().isCompleted()
        && !getSpawns().isEmpty()
        && getSpectator() != null
        && getLobby() != null;
  }

  default Optional<SpawnGroup> getSpawns(@NonNull TeamEnum team) {
    return Optional.ofNullable(getSpawns().get(team));
  }

  static @NonNull World getWorldFromReference(WeakReference<World> reference) {
    Preconditions.checkNotNull(reference, "World reference is null");
    World world = reference.get();
    Preconditions.checkState(world != null, "World has become invalid");
    return world;
  }
}
