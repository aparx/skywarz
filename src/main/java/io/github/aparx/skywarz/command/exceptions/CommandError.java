package io.github.aparx.skywarz.command.exceptions;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.LocalizedMessage;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 13:51
 * @since 1.0
 */
@Getter
public final class CommandError extends RuntimeException {

  private final Function<Language, ?> errorMessageFactory;

  public CommandError(Function<Language, ?> errorMessageFactory) {
    this.errorMessageFactory = errorMessageFactory;
  }

  public CommandError(String message,
                      Function<Language, ?> errorMessageFactory) {
    super(message);
    this.errorMessageFactory = errorMessageFactory;
  }

  public CommandError(String message, Throwable cause,
                      Function<Language, ?> errorMessageFactory) {
    super(message, cause);
    this.errorMessageFactory = errorMessageFactory;
  }

  public CommandError(Throwable cause,
                      Function<Language, ?> errorMessageFactory) {
    super(cause);
    this.errorMessageFactory = errorMessageFactory;
  }

  public CommandError(String message, Throwable cause, boolean enableSuppression,
                      boolean writableStackTrace,
                      Function<Language, ?> errorMessageFactory) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.errorMessageFactory = errorMessageFactory;
  }

  @CanIgnoreReturnValue
  public static <R> R supplyAndRethrowOnError(
      @NonNull Supplier<R> executor,
      @NonNull Function<Language, ?> errorMessageFactory) {
    try {
      return executor.get();
    } catch (Throwable throwable) {
      throw new CommandError(throwable, errorMessageFactory);
    }
  }

  @CanIgnoreReturnValue
  public static void runAndRethrowOnError(
      @NonNull Runnable executor,
      @NonNull Function<Language, ?> errorMessageFactory) {
    try {
      executor.run();
    } catch (Throwable throwable) {
      throw new CommandError(throwable, errorMessageFactory);
    }
  }

  public String createMessage() {
    if (errorMessageFactory == null)
      return null;
    Object object = errorMessageFactory.apply(Language.getInstance());
    if (object instanceof LocalizedMessage)
      return ((LocalizedMessage) object).substitute();
    return Objects.toString(object, null);
  }

}
