package io.github.aparx.skywarz.game.item;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 03:43
 * @since 1.0
 */
@Getter
@Setter
public abstract class StaticGameItem extends GameItem {

  @ConfigMapping("slot")
  @Document("The position of the item in the inventory (0 through 8)")
  private int slot = 0;

  public StaticGameItem(@NonNull String name, @NonNull MatchState[] states) {
    super(name, states);
  }

  public StaticGameItem(@NonNull String name, @NonNull MatchState[] states, int flags) {
    super(name, states, flags);
  }

  public void give(@NonNull Match match, @NonNull Player player) {
    player.getInventory().setItem(getSlot(), create(match, player));
  }

}
