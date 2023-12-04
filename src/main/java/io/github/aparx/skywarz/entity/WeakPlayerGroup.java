package io.github.aparx.skywarz.entity;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 10:54
 * @since 1.0
 */
public class WeakPlayerGroup extends WeakGroupAudience<SkywarsPlayer> {

  public WeakPlayerGroup() {}

  public WeakPlayerGroup(Collection<? extends SkywarsPlayer> initialMembers) {
    super(initialMembers);
  }

  public Stream<? extends SkywarsPlayer> online() {
    return stream().filter(SkywarsPlayer::isOnline);
  }

}