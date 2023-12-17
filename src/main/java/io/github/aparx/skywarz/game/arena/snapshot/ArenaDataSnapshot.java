package io.github.aparx.skywarz.game.arena.snapshot;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.arena.ArenaBox;
import io.github.aparx.skywarz.game.arena.settings.GameSettings;
import io.github.aparx.skywarz.game.arena.IArenaData;
import io.github.aparx.skywarz.game.team.TeamEnum;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Map;

/**
 * A copy of an input {@code IArenaData} implementation.
 * <p>Most attributes are shallow copied, thus can still be deeply mutable except spawns, that
 * are all copied to ensure a constant team size.
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
  private final @NonNull EnumMap<TeamEnum, SpawnGroup> spawns;
  private final @NonNull GameSettings settings;

  public ArenaDataSnapshot(@NonNull IArenaData data) {
    Preconditions.checkNotNull(data, "Data must not be null");
    this.box = data.getBox();
    this.spectator = data.getSpectator();
    this.lobby = data.getLobby();
    this.world = new WeakReference<>(data.getWorld());
    this.settings = data.getSettings();

    // Deep copy
    EnumMap<TeamEnum, SpawnGroup> spawns = data.getSpawns();
    this.spawns = new EnumMap<>(TeamEnum.class);
    for (Map.Entry<TeamEnum, SpawnGroup> entry : spawns.entrySet())
      this.spawns.put(entry.getKey(), entry.getValue().copy());
  }

  public @NonNull World getWorld() {
    return IArenaData.getWorldFromReference(world);
  }

  @Override
  public boolean isCompleted() {
    return IArenaData.super.isCompleted() && world != null && world.get() != null;
  }
}
