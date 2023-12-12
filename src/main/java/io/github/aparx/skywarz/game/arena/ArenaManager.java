package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.handler.DefaultSkywarsHandler;
import io.github.aparx.skywarz.utils.collection.KeyValueSet;
import io.github.aparx.skywarz.utils.collection.KeyValueSets;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 02:51
 * @since 1.0
 */
public final class ArenaManager extends DefaultSkywarsHandler implements Iterable<GameArena> {

  public static final File STORAGE_DIRECTORY_OFFSET = new File(".arenas");

  public static final Pattern NAME_PATTERN = Pattern.compile("[A-z0-9_]+");

  private static final Set<String> FILE_EXTENSIONS = Set.of("yaml", "yml");

  private static String transformKey(String key) {
    return Objects.toString(key).toLowerCase();
  }

  private final KeyValueSet<String, GameArena> internalSet =
      KeyValueSets.of((arena) -> transformKey(arena.getName()));

  public @NonNull File getDirectory() {
    return new File(Skywars.plugin().getDataFolder(), STORAGE_DIRECTORY_OFFSET.getName());
  }

  @Override
  protected void onLoad() {
    File directory = getDirectory();
    if (!directory.exists())
      Preconditions.checkState(directory.mkdirs(), "Could not create directory");
    final File[] files;
    if ((files = directory.listFiles()) != null)
      Arrays.stream(files)
          .filter((file) -> getFileExtension(file).filter(FILE_EXTENSIONS::contains).isPresent())
          .peek((file) -> Skywars.logger().log(Level.INFO, "Found arena file: {0}", file))
          .filter((file) -> NAME_PATTERN.matcher(getFileNameWithoutExtension(file)).matches())
          .forEach((file) -> register(new GameArena(getFileNameWithoutExtension(file), file)));
  }

  private static Optional<String> getFileExtension(File file) {
    int lastIndexOf = file.getName().lastIndexOf('.');
    if (lastIndexOf == -1) return Optional.empty();
    return Optional.of(file.getName().substring(1 + lastIndexOf));
  }

  private static String getFileNameWithoutExtension(File file) {
    int lastIndexOf = file.getName().lastIndexOf('.');
    if (lastIndexOf == -1) return file.getName();
    return file.getName().substring(0, lastIndexOf);
  }

  @Override
  protected void onUnload() {
    internalSet.clear();
  }

  public void register(@NonNull GameArena arena) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    synchronized (handlerLock) {
      Preconditions.checkState(internalSet.add(arena), "Could not add arena (duplicate?)");
      Skywars.logger().log(Level.INFO, "Registered arena: {0}", arena.getName());
    }
  }

  @CanIgnoreReturnValue
  public boolean delete(@NonNull GameArena arena) {
    Preconditions.checkNotNull(arena, "Arena must not be null");
    synchronized (handlerLock) {
      if (!internalSet.remove(arena)) return false;
      Preconditions.checkState(arena.getFile().delete(), "Could not delete arena (used)");
      Skywars.logger().log(Level.INFO, "Deleted arena: {0}", arena.getName());
      return true;
    }
  }

  @CanIgnoreReturnValue
  public boolean delete(@NonNull String name) {
    Preconditions.checkNotNull(name, "Name must not be null");
    synchronized (handlerLock) {
      return find(name).filter(this::delete).isPresent();
    }
  }

  @CheckReturnValue
  public Optional<GameArena> find(@NonNull String name) {
    Preconditions.checkNotNull(name, "Name must not be null");
    synchronized (handlerLock) {
      return internalSet.find(transformKey(name));
    }
  }

  @CanIgnoreReturnValue
  public @NonNull GameArena get(@NonNull String name) {
    Preconditions.checkNotNull(name, "Name must not be null");
    synchronized (handlerLock) {
      return find(name).orElseThrow(() -> new IllegalArgumentException(
          String.format("Arena %s not found", name)));
    }
  }

  public boolean contains(String name) {
    synchronized (handlerLock) {
      return internalSet.containsKey(transformKey(name));
    }
  }

  public boolean contains(GameArena arena) {
    synchronized (handlerLock) {
      return internalSet.contains(arena);
    }
  }

  public @NonNull Stream<GameArena> stream() {
    return internalSet.stream();
  }

  @Override
  public @NonNull Iterator<GameArena> iterator() {
    return internalSet.iterator();
  }

  public Set<GameArena> asSet() {
    return internalSet;
  }

}
