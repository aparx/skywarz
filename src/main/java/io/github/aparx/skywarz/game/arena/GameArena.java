package io.github.aparx.skywarz.game.arena;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.bufig.defaults.yaml.YamlConfig;
import io.github.aparx.skywarz.Magics;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.game.SpawnGroup;
import io.github.aparx.skywarz.game.arena.reset.ArenaReset;
import io.github.aparx.skywarz.game.arena.reset.DefaultArenaReset;
import io.github.aparx.skywarz.game.arena.settings.ArenaSettings;
import io.github.aparx.skywarz.game.arena.sign.ArenaSignHandler;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import io.github.aparx.skywarz.setup.CompletableSetup;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 01:38
 * @since 1.0
 */
@Getter
public final class GameArena extends ConfigObject implements CompletableSetup {

  @Setter(AccessLevel.NONE)
  private final @NonNull String name;

  @ConfigMapping
  @Document("The data of this arena, do not modify unless you know the consequences!")
  private @NonNull ArenaData data;

  private @NonNull ArenaReset reset = new DefaultArenaReset(this);

  private final ArenaSignHandler signHandler = new ArenaSignHandler(this);

  public GameArena(@NonNull String initialName) {
    this(initialName, createArenaFile(initialName));
  }

  public GameArena(@NonNull String initialName, @NonNull File file) {
    super((proxy) -> Skywars.getInstance().getConfigHandler()
        .getOrCreate(file.getPath(), (configId) -> new YamlConfig(configId, file)));
    Validate.notEmpty(initialName, "Initial name must not be blank");
    this.load();
    if (data == null) data = new ArenaData();
    this.name = Objects.toString(get("name"), initialName);
    Preconditions.checkState(data != null, "Data is null");
    setHeaderIfAbsent(SkywarsConfigHandler.createHeader(String.format("Arena %s (do not touch!)", name)));
    this.save();
  }

  public static File createArenaFile(@NonNull String pureName) {
    return new File(Skywars.getInstance().getArenaManager().getDirectory(), pureName + ".yml");
  }

  public static int getMinPlayerCount(@NonNull ArenaSettings settings) {
    return (Magics.isDevelopment() ? 0 : 1) + settings.getTeamSize();
  }

  public static int getMaxPlayerCount(@NonNull ArenaSettings settings, int teamCount) {
    return teamCount * settings.getTeamSize();
  }

  public static int getAvailableTeamCount(@NonNull GameArena arena) {
    int teamCount = 0; // approximate max team count (may change until match is created!)
    for (TeamEnum teamEnum : TeamEnum.values())
      if (arena.getData().getSpawns(teamEnum)
          .filter(Predicate.not(SpawnGroup::isEmpty))
          .isPresent())
        ++teamCount;
    return teamCount;
  }

  @Override
  public void load() {
    super.load();
    signHandler.load();
  }

  @Override
  public void save() {
    signHandler.save();
    super.save();
    if (signHandler.getRegister() != null)
      signHandler.update();
    findAcquiringMatch().ifPresent((acquiree) -> {
      // enforce removal of match if arena is updated
      acquiree.getWatchTask().tick(TickDuration.of());
    });
  }

  public boolean isAcquiredByMatch() {
    return findAcquiringMatch().isPresent();
  }

  public Optional<GameMatch> findAcquiringMatch() {
    return Skywars.getInstance().getMatchManager().find(this);
  }

  public void setReset(@NonNull ArenaReset reset) {
    Preconditions.checkNotNull(reset, "Reset must not be null");
    Preconditions.checkState(!isAcquiredByMatch(), "Arena is currently acquired by a match");
    this.reset = reset;
  }

  @Override
  public boolean isCompleted() {
    return data.getBox().isCompleted()
        && data.getLobby() != null
        && data.getSpectator() != null
        && getAvailableTeamCount(this) > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GameArena arena = (GameArena) o;
    return Objects.equals(name, arena.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

}
