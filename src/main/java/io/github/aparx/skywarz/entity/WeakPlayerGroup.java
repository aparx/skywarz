package io.github.aparx.skywarz.entity;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 10:54
 * @since 1.0
 */
public class WeakPlayerGroup extends WeakGroupAudience<GamePlayer> {

  public WeakPlayerGroup() {}

  public WeakPlayerGroup(Collection<? extends GamePlayer> initialMembers) {
    super(initialMembers);
  }

  public Stream<? extends GamePlayer> online() {
    return stream().filter(GamePlayer::isOnline);
  }

  /** Returns a stream of all online and alive players (non-spectators) */
  public Stream<? extends GamePlayer> alive() {
    return online().filter((player) -> !player.getMatchData().isSpectator());
  }

  /** Returns a stream of all online and dead players (spectators) */
  public Stream<? extends GamePlayer> dead() {
    return online().filter((player) -> player.getMatchData().isSpectator());
  }

  /** Returns a stream of all online and dead players (spectators) */
  public Stream<? extends Player> entity() {
    return online().map(GamePlayer::findOnline).filter(Optional::isPresent).map(Optional::get);
  }

  public int getOnlineCount() {
    return (int) online().count();
  }

  public int getAliveCount() {
    return (int) alive().count();
  }

}
