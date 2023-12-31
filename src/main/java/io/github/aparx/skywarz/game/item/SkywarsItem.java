package io.github.aparx.skywarz.game.item;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.match.GameMatchState;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.swing.text.StyleContext;
import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 06:52
 * @since 1.0
 */
@Getter
public abstract class SkywarsItem extends ConfigObject {

  private final @NonNull GameMatchState[] states;

  private final int flags;

  @Getter
  private final String itemID = UUID.randomUUID().toString();

  public SkywarsItem(@NonNull String name, @NonNull GameMatchState[] states) {
    this(name, states, 0);
  }

  public SkywarsItem(@NonNull String name, @NonNull GameMatchState[] states, int flags) {
    super((proxy) -> Skywars.getInstance().getConfigHandler().getOrCreate("items/" + name));
    Preconditions.checkNotNull(states, "States must not be null");
    Validate.noNullElements(states, "State must not be null");
    Preconditions.checkState((flags & ~Flags.FLAGS) == 0, "Unknown flags");
    this.states = (GameMatchState[]) ArrayUtils.clone(states);
    this.flags = flags;
  }

  /**
   * Creates a new itemstack representing this {@code GameItem}.
   * <p>The returned stack <strong>will</strong> be modified, thus a copy may be required.
   *
   * @param match     the match for the itemstack
   * @param initiator the initiator (user) of this item
   * @return the itemstack, {@code not null}
   */
  protected abstract ItemStack createItemStack(@NonNull GameMatch match, @NonNull Player initiator);

  private void applyItemIdentifier(ItemMeta itemMeta) {

  }

  @CanIgnoreReturnValue
  public final ItemStack create(@NonNull GameMatch match, @NonNull Player initiator) {
    ItemStack stack = createDummy(match, initiator);
    ItemMeta meta = stack.getItemMeta();
    Preconditions.checkNotNull(meta, "Item has no meta");
    meta.getPersistentDataContainer().set(getStampKey(), PersistentDataType.STRING, itemID);
    stack.setItemMeta(meta);
    return stack;
  }

  @CanIgnoreReturnValue
  public final ItemStack createDummy(@NonNull GameMatch match, @NonNull Player initiator) {
    Preconditions.checkNotNull(match, "Match must not be null");
    Preconditions.checkNotNull(initiator, "Player must not be null");
    Preconditions.checkState(Arrays.stream(states).anyMatch(match::isState),
        "Cannot create item at state of match");
    return createItemStack(match, initiator);
  }

  public boolean isItem(ItemStack itemStack) {
    if (itemStack == null) return false;
    ItemMeta meta = itemStack.getItemMeta();
    if (meta == null) return false;
    return itemID.equals(meta.getPersistentDataContainer()
        .get(getStampKey(), PersistentDataType.STRING));
  }

  public void register() {
    load();
  }

  public void unregister() {}

  /** Event method called whenever a player interacts with this {@code GameItem} */
  protected void handleClick(@NonNull GameMatch match, PlayerInteractEvent event) {}

  /** Event method called whenever a player drops this {@code GameItem} */
  protected void handleDrop(@NonNull GameMatch match, PlayerDropItemEvent event) {
    event.setCancelled(!Flags.isDropable(getFlags()));
  }

  /** Event method called whenever a player moves this {@code GameItem} in an inventory */
  protected void handleInventory(@NonNull GameMatch match, InventoryInteractEvent event) {
    event.setCancelled(!Flags.isMovable(getFlags()));
  }

  protected Optional<GameMatch> filterMatch(Player player) {
    return SkywarsPlayer.findPlayer(player)
        .map(SkywarsPlayer::getMatchData)
        .filter(PlayerMatchData::isInMatch)
        .map(PlayerMatchData::getMatch)
        .filter((match) -> Arrays.stream(states).anyMatch(match::isState));
  }

  private NamespacedKey getStampKey() {
    return new NamespacedKey(Skywars.plugin(), "gameItem");
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
