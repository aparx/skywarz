package io.github.aparx.skywarz.command.exceptions;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.handler.configs.Language;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 06:01
 * @since 1.0
 */
@Getter
public final class CommandError extends RuntimeException {

  private final @Nullable BiFunction<Throwable, Language, String> errorMessageFactory;

  public CommandError(@Nullable BiFunction<Throwable, Language, String> errorMessageFactory) {
    this((String) null, errorMessageFactory);
  }

  public CommandError(
      String message,
      @Nullable BiFunction<Throwable, Language, String> errorMessageFactory) {
    super(message);
    this.errorMessageFactory = errorMessageFactory;
  }

  public CommandError(
      String message, Throwable cause,
      @Nullable BiFunction<Throwable, Language, String> errorMessageFactory) {
    super(message, cause);
    this.errorMessageFactory = errorMessageFactory;
  }

  public CommandError(
      Throwable cause,
      @Nullable BiFunction<Throwable, Language, String> errorMessageFactory) {
    super(cause);
    this.errorMessageFactory = errorMessageFactory;
  }

  public String getMessageFromLanguage(@NonNull Language language) {
    if (errorMessageFactory == null) return null;
    return errorMessageFactory.apply(getCause() != null ? getCause() : this, language);
  }

  public String getMessageFromLanguage() {
    return getMessageFromLanguage(Language.getLanguage());
  }

  @CanIgnoreReturnValue
  public static <R> R supplyAndRethrowOnError(
      @NonNull Supplier<R> executor,
      @NonNull BiFunction<Throwable, @NonNull Language, String> errorMessageFactory) {
    try {
      return executor.get();
    } catch (Throwable throwable) {
      throw new CommandError(throwable, errorMessageFactory);
    }
  }

  @CanIgnoreReturnValue
  public static void runAndRethrowOnError(
      @NonNull Runnable executor,
      @NonNull BiFunction<Throwable, @NonNull Language, String> errorMessageFactory) {
    try {
      executor.run();
    } catch (Throwable throwable) {
      throw new CommandError(throwable, errorMessageFactory);
    }
  }

}
