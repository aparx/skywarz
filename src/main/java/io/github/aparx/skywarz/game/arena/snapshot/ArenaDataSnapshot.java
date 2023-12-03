package io.github.aparx.skywarz.game.arena.snapshot;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.arena.ArenaBox;
import io.github.aparx.skywarz.game.arena.GameSettings;
import io.github.aparx.skywarz.game.arena.IArenaData;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * A shallow copy of an input {@code IArenaData} implementation.
 *
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 09:01
 * @since 1.0
 */
@Getter
public class ArenaDataSnapshot implements IArenaData {

  private final @NonNull ArenaBox box;
  private final Location spectator;
  private final Location lobby;
  private final WeakReference<World> world;
  private final @NonNull Map<String, SpawnGroup> spawns;
  private final @NonNull GameSettings settings;

  public ArenaDataSnapshot(@NonNull IArenaData data) {
    Preconditions.checkNotNull(data, "Data must not be null");
    this.box = data.getBox();
    this.spectator = data.getSpectator();
    this.lobby = data.getLobby();
    this.world = new WeakReference<>(data.getWorld());
    this.spawns = data.getSpawns();
    this.settings = data.getSettings();
  }

  public @NonNull World getWorld() {
    return IArenaData.getWorldFromReference(world);
  }
}
