package io.github.aparx.skywarz.language;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.bufig.ArrayPath;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.text.lookup.StringLookup;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 09:39
 * @since 1.0
 */
@Getter
public class MessageRegister implements MessageStorage<ArrayPath> {

  @Getter(AccessLevel.NONE)
  private final Map<ArrayPath, LocalizedMessage> messages = new LinkedHashMap<>();

  private final StringLookup messagesLookup = (variable) -> {
    LocalizedMessage localizedMessage = messages.get(ArrayPath.parse(variable));
    if (localizedMessage != null)
      return localizedMessage.getRawContent();
    return null;
  };

  public Set<Map.Entry<ArrayPath, LocalizedMessage>> entrySet() {
    return messages.entrySet();
  }

  @Override
  public Optional<LocalizedMessage> find(@NonNull ArrayPath path) {
    Preconditions.checkNotNull(path, "Path must not be null");
    return Optional.ofNullable(messages.get(path));
  }

  @Override
  public @NonNull LocalizedMessage get(@NonNull ArrayPath path) {
    Preconditions.checkNotNull(path, "Path must not be null");
    LocalizedMessage message = messages.get(path);
    if (LocalizedMessage.isEmpty(message))
      return new LocalizedMessage(messagesLookup, path.join());
    return message;
  }

  @Override
  @CanIgnoreReturnValue
  public @Nullable LocalizedMessage store(
      @NonNull ArrayPath path, @NonNull LocalizedMessage message) {
    Preconditions.checkNotNull(path, "Path must not be null");
    Preconditions.checkNotNull(message, "Message must not be null");
    Preconditions.checkArgument(!path.isEmpty(), "Path must not be empty");
    return messages.put(path, message);
  }

  @Override
  @CanIgnoreReturnValue
  public @Nullable LocalizedMessage store(@NonNull ArrayPath path, String message) {
    Preconditions.checkNotNull(path, "Path must not be null");
    Preconditions.checkNotNull(message, "Message must not be null");
    return store(path, new LocalizedMessage(messagesLookup, message));
  }

  @Override
  @CanIgnoreReturnValue
  public @Nullable LocalizedMessage store(@NonNull ArrayPath path, Object message) {
    return store(path, Objects.toString(message, null));
  }

}
