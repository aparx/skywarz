package io.github.aparx.skywarz.language;

import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.bufig.handler.ConfigProxy;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.team.TeamEnum;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 09:38
 * @since 1.0
 */
@Getter
public final class Language implements MessageStorage<ArrayPath> {

  @Getter
  private static final Language instance = new Language();

  private final MessageRegister register = new MessageRegister();

  private final ConfigProxy config = new ConfigProxy((proxy) -> {
    // returns a config targeting the messages file (that is deterministic)
    return Skywars.getInstance().getConfigHandler().getOrCreate("messages");
  });

  public Language() {
    MessageKeys.defaultMessages.forEach(this::store);
  }

  public void load() {
    config.load();
    // copy over message values set in the config to the register
    for (String key : config.getKeys(true)) {
      ArrayPath path = ArrayPath.parse(key, ArrayPath.DEFAULT_SEPARATOR);
      Object object = config.get(path);
      if (object instanceof String)
        register.store(path, object);
    }
    save();
  }

  public void save() {
    // copy over all messages from the register to the config
    register.entrySet().forEach((entry) -> {
      config.set(entry.getKey(), entry.getValue().getRawContent());
    });
    config.save();
  }

  @CheckReturnValue
  public LocalizedMessage localize(@NonNull String content) {
    return new LocalizedMessage(register.getMessagesLookup(), content);
  }

  @CheckReturnValue
  public String substitute(String content) {
    return localize(content).substitute();
  }

  @CheckReturnValue
  public String substitute(String content, Map<String, ?> data) {
    return localize(content).substitute(data);
  }

  @CheckReturnValue
  public String substitute(String content, Object... args) {
    return localize(content).substitute(args);
  }

  @CheckReturnValue
  public String substitute(ArrayPath path) {
    return find(path).map(LocalizedMessage::substitute).orElseGet(path::join);
  }

  @CheckReturnValue
  public String substitute(ArrayPath path, Map<String, ?> data) {
    return find(path).map((msg) -> msg.substitute(data)).orElseGet(path::join);
  }

  @CheckReturnValue
  public String substitute(ArrayPath path, Object... args) {
    return find(path).map((msg) -> msg.substitute(args)).orElseGet(path::join);
  }

  @Override
  public Optional<LocalizedMessage> find(@NonNull ArrayPath path) {
    return register.find(path);
  }

  @Override
  public @NonNull LocalizedMessage get(@NonNull ArrayPath path) {
    return register.get(path);
  }

  @Override
  public @Nullable LocalizedMessage store(
      @NonNull ArrayPath path, @NonNull LocalizedMessage message) {
    return register.store(path, message);
  }

  @Override
  public @Nullable LocalizedMessage store(@NonNull ArrayPath path, String message) {
    return register.store(path, message);
  }

  @Override
  public @Nullable LocalizedMessage store(@NonNull ArrayPath path, Object message) {
    return register.store(path, message);
  }

  public String getTeamName(TeamEnum teamEnum) {
    return teamEnum.getDefaultName(); // TODO
  }
}
