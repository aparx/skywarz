package io.github.aparx.skywarz.game.arena.reset;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.arena.GameArena;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 05:39
 * @since 1.0
 */
public abstract class ArenaReset {

  private final WeakReference<GameArena> arena;

  public ArenaReset(@NonNull GameArena arena) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    this.arena = new WeakReference<>(arena);
  }

  /**
   * Invoked when an arena should be made ready for the underlying reset to begin capturing
   * events happening.
   */
  public abstract void capture();

  public abstract void reset();

  public GameArena getArena() {
    GameArena arena = this.arena.get();
    Preconditions.checkState(arena != null, "Arena became invalid");
    return arena;
  }
}
