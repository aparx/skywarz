package io.github.aparx.skywarz.game.scoreboard;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.ObjIntConsumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-08 10:43
 * @since 1.0
 */
public final class ScoreboardContent {

  @Getter
  private final @NonNull String title;
  private final @Nullable String @NonNull [] lines;

  private ScoreboardContent(@NonNull String title, @Nullable String @NonNull [] lines) {
    Preconditions.checkNotNull(title, "Title must not be null");
    Preconditions.checkNotNull(lines, "Lines must not be null");
    this.title = title;
    this.lines = lines;
  }

  public static ScoreboardContentBuilder builder() {
    return new ScoreboardContentBuilder();
  }

  /** Ignores the immutability aspect of {@code ScoreboardContent} */
  public static ScoreboardContent delegate(@NonNull String title, @Nullable String... lines) {
    return new ScoreboardContent(title, ArrayUtils.nullToEmpty(lines));
  }

  public static ScoreboardContent copyOf(@NonNull String title, @Nullable String... lines) {
    return new ScoreboardContent(title, ArrayUtils.nullToEmpty(ArrayUtils.clone(lines)));
  }

  public int length() {
    return lines.length;
  }

  @CheckReturnValue
  public @Nullable String get(@NonNegative int index) {
    Preconditions.checkElementIndex(index, length());
    return lines[index];
  }

  public @Nullable String @NonNull [] toArray() {
    return ArrayUtils.clone(lines);
  }

  public Iterator<String> iterator() {
    return Arrays.stream(lines).iterator();
  }

  public void forEach(@NonNull ObjIntConsumer<String> action) {
    Preconditions.checkNotNull(action);
    for (int i = 0; i < lines.length; ++i)
      action.accept(lines[i], i);
  }

  @Getter
  @Setter
  @Accessors(fluent = true)
  public static final class ScoreboardContentBuilder {

    private final ChatColor[] colors = ChatColor.values();

    @Setter(AccessLevel.NONE)
    private @NonNull LinkedHashSet<String> lines = new LinkedHashSet<>();
    private @Nullable String title;

    private volatile int colorCodeGenerationCursor = 0;

    public synchronized ScoreboardContent build() {
      return new ScoreboardContent(title == null ? StringUtils.SPACE : title,
          lines.toArray(String[]::new));
    }

    @CanIgnoreReturnValue
    public synchronized ScoreboardContentBuilder lines(String... lines) {
      lines = ArrayUtils.nullToEmpty(lines);
      this.colorCodeGenerationCursor = 0;
      this.lines = new LinkedHashSet<>(lines.length);
      for (String line : lines)
        appendLine(line);
      return this;
    }

    @CanIgnoreReturnValue
    public synchronized ScoreboardContentBuilder appendLine(String line) {
      if (StringUtils.isBlank(line))
        return appendEmpty();
      lines.add(lines.contains(line) ? generateNextUniqueColorId() + line : line);
      return this;
    }

    @CanIgnoreReturnValue
    public synchronized ScoreboardContentBuilder appendEmpty() {
      lines.add(generateNextUniqueColorId());
      return this;
    }

    @CanIgnoreReturnValue
    public synchronized ScoreboardContentBuilder appendEmpty(int repeat) {
      while (--repeat >= 0)
        lines.add(generateNextUniqueColorId());
      return this;
    }

    /**
     * Creates a new sequence of colors based off of previous generated colors, to enable
     * duplicate-like lines to exist within a scoreboard. This color ID is used as the prefix or
     * suffix to enable duplicates.
     *
     * @return the newly generated color ID (will affect successive identifiers)
     */
    @CheckReturnValue
    public synchronized String generateNextUniqueColorId() {
      StringBuilder builder = new StringBuilder();
      int repeat = 1 + (colorCodeGenerationCursor / colors.length);
      for (int i = 0; i < repeat; ++i)
        builder.append(colors[(colorCodeGenerationCursor++) % colors.length]);
      builder.append(ChatColor.RESET);
      return builder.toString();
    }

  }
}
