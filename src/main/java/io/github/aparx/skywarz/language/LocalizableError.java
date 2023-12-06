package io.github.aparx.skywarz.language;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
public final class LocalizableError extends RuntimeException {

  private final Function<Language, ?> errorMessageFactory;

  public LocalizableError(Function<Language, ?> errorMessageFactory) {
    this.errorMessageFactory = errorMessageFactory;
  }

  public LocalizableError(String message,
                          Function<Language, ?> errorMessageFactory) {
    super(message);
    this.errorMessageFactory = errorMessageFactory;
  }

  public LocalizableError(String message, Throwable cause,
                          Function<Language, ?> errorMessageFactory) {
    super(message, cause);
    this.errorMessageFactory = errorMessageFactory;
  }

  public LocalizableError(Throwable cause,
                          Function<Language, ?> errorMessageFactory) {
    super(cause);
    this.errorMessageFactory = errorMessageFactory;
  }

  public LocalizableError(String message, Throwable cause, boolean enableSuppression,
                          boolean writableStackTrace,
                          Function<Language, ?> errorMessageFactory) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.errorMessageFactory = errorMessageFactory;
  }

  @CanIgnoreReturnValue
  public static <R> R localizeThrow(
      @NonNull Supplier<R> executor,
      @NonNull Function<Language, ?> errorMessageFactory) {
    try {
      return executor.get();
    } catch (Throwable throwable) {
      throw new LocalizableError(throwable, errorMessageFactory);
    }
  }

  @Override
  public String getLocalizedMessage() {
    return createMessage();
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
