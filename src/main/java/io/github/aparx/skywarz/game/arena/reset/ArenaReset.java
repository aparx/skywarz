package io.github.aparx.skywarz.game.arena.reset;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.arena.SkywarsArena;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.ref.WeakReference;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 05:39
 * @since 1.0
 */
public abstract class ArenaReset {

  private final WeakReference<SkywarsArena> arena;

  public ArenaReset(@NonNull SkywarsArena arena) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    this.arena = new WeakReference<>(arena);
  }

  /**
   * Invoked when an arena should be made ready for the underlying reset to begin capturing
   * events happening.
   */
  public abstract void capture();

  public abstract void reset();

  public SkywarsArena getArena() {
    SkywarsArena arena = this.arena.get();
    Preconditions.checkState(arena != null, "Arena became invalid");
    return arena;
  }
}
