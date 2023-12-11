package io.github.aparx.skywarz.command.arguments;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 05:40
 * @since 1.0
 */
public final class CommandArgList implements Iterable<CommandArgument> {

  private static final String DEFAULT_JOIN_DELIMITER = StringUtils.SPACE;

  private static final CommandArgList EMPTY =
      new CommandArgList(ArrayUtils.EMPTY_STRING_ARRAY);

  private final @NonNull String @NonNull [] rawArgs;

  private final CommandArgument @NonNull [] mappedArgs;

  private CommandArgList(@NonNull String @NonNull [] args) {
    this.rawArgs = args;
    this.mappedArgs = new CommandArgument[args.length];
  }

  public static CommandArgList of() {
    return EMPTY;
  }

  @CheckReturnValue
  public static CommandArgList of(@NonNull String arg0) {
    Preconditions.checkNotNull(arg0, "Argument must not be null");
    return new CommandArgList(new String[]{arg0});
  }

  @CheckReturnValue
  public static CommandArgList of(@NonNull String arg0, @NonNull String arg1) {
    Preconditions.checkNotNull(arg0, "First argument must not be null");
    Preconditions.checkNotNull(arg1, "Second argument must not be null");
    return new CommandArgList(new String[]{arg0, arg1});
  }

  @CheckReturnValue
  public static CommandArgList of(@NonNull String arg0, @NonNull String... argN) {
    return of((String[]) ArrayUtils.add(argN, 0, arg0));
  }

  @CheckReturnValue
  public static CommandArgList of(@NonNull String[] args) {
    Preconditions.checkNotNull(args, "Arguments must not be null");
    if (args.length == 0) return of();
    if (args.length == 1) return of(args[0]);
    Validate.noNullElements(args, "Arguments must not contain a null argument");
    return delegate(args);
  }

  public static CommandArgList parse(@NonNull String commandLine) {
    Preconditions.checkNotNull(commandLine, "Line must not be null");
    return of(commandLine.split(StringUtils.SPACE));
  }

  @CheckReturnValue
  public static CommandArgList copyOf(@NonNull Iterable<String> iterable) {
    Preconditions.checkNotNull(iterable, "Iterable must not be null");
    ArrayList<String> resizableArray = new ArrayList<>();
    for (String string : iterable)
      resizableArray.add(Preconditions.checkNotNull(string));
    return delegate(resizableArray.toArray(String[]::new));
  }

  @CheckReturnValue
  public static CommandArgList copyOf(@NonNull Collection<String> collection) {
    Preconditions.checkNotNull(collection, "Collection must not be null");
    Validate.noNullElements(collection, "Collection must not contain null elements");
    return delegate(collection.toArray(String[]::new));
  }


  private static CommandArgList delegate(String[] args) {
    return args.length == 0 ? EMPTY : new CommandArgList(args);
  }

  public int length() {
    return rawArgs.length;
  }

  public boolean isEmpty() {
    return rawArgs.length == 0;
  }

  public int indexOf(String argument) {
    return ArrayUtils.indexOf(rawArgs, argument);
  }

  public boolean contains(String argument) {
    return ArrayUtils.contains(rawArgs, argument);
  }

  public @NonNull CommandArgument last() {
    Preconditions.checkState(!isEmpty(), "No arguments present");
    return get(length() - 1);
  }

  public @NonNull CommandArgument first() {
    Preconditions.checkState(!isEmpty(), "No arguments present");
    return get(0);
  }

  @CanIgnoreReturnValue
  public @NonNull CommandArgument get(@NonNegative int index) {
    Preconditions.checkElementIndex(index, rawArgs.length);
    CommandArgument argument = mappedArgs[index];
    if (argument == null)
      argument = new CommandArgument(this, index, rawArgs[index]);
    mappedArgs[index] = argument;
    return argument;
  }

  @CanIgnoreReturnValue
  public @NonNull String getString(@NonNegative int index) {
    Preconditions.checkElementIndex(index, rawArgs.length);
    return rawArgs[index];
  }

  public @NonNull CommandArgList subargs(int fromInclusiveIndex, int toExclusiveIndex) {
    Preconditions.checkPositionIndex(fromInclusiveIndex, length());
    Preconditions.checkPositionIndex(toExclusiveIndex, length());
    return delegate(Arrays.copyOfRange(rawArgs, fromInclusiveIndex, toExclusiveIndex));
  }

  public @NonNull CommandArgList subargs(int fromInclusiveIndex) {
    return subargs(fromInclusiveIndex, length());
  }

  public String join(@NonNull String separator) {
    return String.join(separator, rawArgs);
  }

  public String join() {
    return String.join(DEFAULT_JOIN_DELIMITER, rawArgs);
  }

  public String join(@NonNull String delimiter, int fromInclusiveIndex, int toExclusiveIndex) {
    Preconditions.checkNotNull(delimiter, "Delimiter must not be null");
    Preconditions.checkElementIndex(fromInclusiveIndex, length());
    Preconditions.checkPositionIndex(toExclusiveIndex, length());
    Preconditions.checkState(toExclusiveIndex >= fromInclusiveIndex,
        "fromInclusiveIndex > toExclusiveIndex", fromInclusiveIndex, toExclusiveIndex);
    StringBuilder builder = new StringBuilder();
    for (; fromInclusiveIndex < toExclusiveIndex; ++fromInclusiveIndex) {
      if (builder.length() != 0) builder.append(delimiter);
      builder.append(rawArgs[fromInclusiveIndex]);
    }
    return builder.toString();
  }

  public String join(@NonNull String delimiter, int fromInclusiveIndex) {
    return join(delimiter, fromInclusiveIndex, length());
  }

  public String join(int fromInclusiveIndex, int toExclusiveIndex) {
    return join(DEFAULT_JOIN_DELIMITER, fromInclusiveIndex, toExclusiveIndex);
  }

  public String join(int fromInclusiveIndex) {
    return join(DEFAULT_JOIN_DELIMITER, fromInclusiveIndex, length());
  }

  public @NonNull String @NonNull [] toStringArray() {
    return (String[]) ArrayUtils.clone(rawArgs);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CommandArgList that = (CommandArgList) o;
    return Arrays.equals(rawArgs, that.rawArgs);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(rawArgs);
  }


  @Override
  public String toString() {
    return "CommandArgList{rawArgs=" + Arrays.toString(rawArgs) + '}';
  }

  @Override
  public @NonNull Iterator<CommandArgument> iterator() {
    return new Iterator<>() {

      int cursor;

      @Override
      public boolean hasNext() {
        return cursor < length();
      }

      @Override
      public CommandArgument next() {
        return get(cursor++);
      }
    };
  }

  public @NonNull Stream<CommandArgument> stream() {
    return StreamSupport.stream(Spliterators.spliterator(iterator(), length(),
        Spliterator.SIZED | Spliterator.NONNULL), false);
  }
}
