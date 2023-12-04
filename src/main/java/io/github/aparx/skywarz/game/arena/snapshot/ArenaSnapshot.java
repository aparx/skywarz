package io.github.aparx.skywarz.game.arena.snapshot;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.game.arena.Arena;
import io.github.aparx.skywarz.setup.CompletableSetup;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 09:29
 * @since 1.0
 */
@Getter
public class ArenaSnapshot implements CompletableSetup {

  private final @NonNull WeakReference<Arena> source;
  private final @NonNull String name;
  private final @NonNull ArenaDataSnapshot data;

  public ArenaSnapshot(@NonNull Arena source) {
    Preconditions.checkNotNull(source, "Source must not be null");
    this.source = new WeakReference<>(source);
    this.name = source.getName();
    this.data = new ArenaDataSnapshot(source.getData());
  }

  public @Nullable Arena getSource() {
    return source.get();
  }

  @Override
  public boolean isCompleted() {
    return data.isCompleted();
  }
}
