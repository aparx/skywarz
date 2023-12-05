package io.github.aparx.skywarz.game.items;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.utils.collection.WeakHashSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 06:52
 * @since 1.0
 */
@Getter
public abstract class GameItem extends ConfigObject {

  private final WeakHashSet<ItemStack> register = new WeakHashSet<>();

  private final @NonNull MatchState state;

  private final int flags;

  public GameItem(@NonNull String name, @NonNull MatchState state) {
    this(name, state, 0);
  }

  public GameItem(@NonNull String name, @NonNull MatchState state, int flags) {
    super((proxy) -> Skywars.getInstance().getConfigHandler().getOrCreate("items/" + name));
    Preconditions.checkNotNull(state, "State must not be null");
    Preconditions.checkState((flags & ~Flags.FLAGS) == 0, "Unknown flags");
    this.state = state;
    this.flags = flags;
  }

  protected abstract ItemStack createItemStack(@NonNull Match match, @NonNull Player initiator);

  @CanIgnoreReturnValue
  public final ItemStack create(@NonNull Match match, @NonNull Player initiator) {
    Preconditions.checkNotNull(match, "Match must not be null");
    Preconditions.checkNotNull(initiator, "Player must not be null");
    Preconditions.checkState(match.isState(getState()), "Cannot create item at state of match");
    ItemStack stack = createItemStack(match, initiator);
    register.add(stack);
    return stack;
  }

  public boolean isItem(ItemStack itemStack) {
    return register.contains(itemStack);
  }

  public void register() {}

  public void unregister() {}

  /** Event method called whenever a player interacts with this {@code GameItem} */
  protected void handleClick(@NonNull Match match, PlayerInteractEvent event) {}

  /** Event method called whenever a player drops this {@code GameItem} */
  protected void handleDrop(@NonNull Match match, PlayerDropItemEvent event) {
    event.setCancelled(!Flags.isDropable(getFlags()));
  }

  /** Event method called whenever a player moves this {@code GameItem} in an inventory */
  protected void handleInventory(@NonNull Match match, InventoryInteractEvent event) {
    event.setCancelled(!Flags.isMovable(getFlags()));
  }

  protected Optional<Match> filterMatch(Player player) {
    return SkywarsPlayer.findPlayer(player)
        .map(SkywarsPlayer::getMatchData)
        .filter(PlayerMatchData::isInMatch)
        .map(PlayerMatchData::getMatch)
        .filter((match) -> match.isState(getState()));
  }

  @UtilityClass
  public static final class Flags {
    public static final int MOVABLE = 1;
    public static final int DROPABLE = 2;
    private static final int FLAGS = MOVABLE | DROPABLE;

    public static boolean isMovable(int flags) {
      return (flags & MOVABLE) != 0;
    }

    public static boolean isDropable(int flags) {
      return (flags & DROPABLE) != 0;
    }
  }

}
