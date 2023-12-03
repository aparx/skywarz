package io.github.aparx.skywarz.entity.data.types;

import io.github.aparx.skywarz.entity.data.SkywarsPlayerData;
import lombok.Getter;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-03 05:14
 * @since 1.0
 */
@Getter
public final class PlayerStatisticData extends SkywarsPlayerData {

  private final AtomicInteger kills = new AtomicInteger();

  private final AtomicInteger deaths = new AtomicInteger();

  private final AtomicInteger matchesPlayed = new AtomicInteger();

  private final AtomicInteger matchesWon = new AtomicInteger();

  public @NonNegative int getMatchesLost() {
    return Math.max(getMatchesPlayed().get() - getMatchesWon().get(), 0);
  }

}
