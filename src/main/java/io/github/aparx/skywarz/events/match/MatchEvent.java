package io.github.aparx.skywarz.events.match;

import io.github.aparx.skywarz.game.match.SkywarsMatch;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 06:34
 * @since 1.0
 */
@Getter
public class MatchEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  protected SkywarsMatch match;

  public MatchEvent(SkywarsMatch match) {
    this.match = match;
  }

  @Override
  public @NonNull HandlerList getHandlers() {
    return handlerList;
  }
}
