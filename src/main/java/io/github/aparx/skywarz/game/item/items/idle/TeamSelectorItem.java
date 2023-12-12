package io.github.aparx.skywarz.game.item.items.idle;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.skywarz.entity.GamePlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.inventory.*;
import io.github.aparx.skywarz.game.inventory.content.InventoryPage;
import io.github.aparx.skywarz.game.item.StaticSkywarsItem;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.match.GameMatchState;
import io.github.aparx.skywarz.game.team.GameTeam;
import io.github.aparx.skywarz.game.team.TeamMap;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import io.github.aparx.skywarz.utils.material.ColoredMaterial;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 06:57
 * @since 1.0
 */
@Document("Team Selector")
public final class TeamSelectorItem extends StaticSkywarsItem {

  private static final ColoredMaterial REPLACEABLE_MATERIAL = ColoredMaterial.CONCRETE;

  private final TickDuration INVENTORY_UPDATE_INTERVAL = TickDuration.of(TimeUnit.TICKS, 10);

  @ConfigMapping
  @Document({
      "The item with which a player can interact. Use bed, concrete or wool as a type",
      "to have it replaced with the color of the team a player has joined (it being the default)."
  })
  private WrappedItemStack item = ItemBuilder
      .builder(Material.WHITE_BED)
      .lore(ChatColor.DARK_GRAY + "Click to select your team")
      .name(ChatColor.AQUA + "Team selector")
      .enchants(Map.of(Enchantment.ARROW_KNOCKBACK, 2))
      .flags(ItemFlag.HIDE_ENCHANTS)
      .wrap();

  @ConfigMapping("menu.title")
  @Document("The title of the team selector inventory")
  private String menuTitle = "Team Selector";

  @ConfigMapping("menu.team.status.joined")
  @Document("Applied to a team item to show a player that they have already joined the team")
  private String teamStatusJoined = "Joined this team";

  @ConfigMapping("menu.team.status.full")
  @Document("Applied to a team item to indicate that the team is already full")
  private String teamStatusFull = "This team is full";

  @ConfigMapping("menu.team.status.joinable")
  @Document("Applied to a team item to indicate the team to be joinable")
  private String teamStatusJoinable = "Click to join this team";

  @ConfigMapping("menu.team.member")
  @Document("Applied to a team item to showcase who joined a team")
  private String teamMemberSlot = "§8• {0}";

  public TeamSelectorItem() {
    super("team selector", new GameMatchState[]{GameMatchState.IDLE});
    setSlot(1);
  }

  @Override
  protected ItemStack createItemStack(@NonNull GameMatch match, @NonNull Player initiator) {
    ItemStack stack = item.getStack().clone();
    Optional.ofNullable(ColoredMaterial.getColored(stack.getType()))
        .ifPresent((colored) -> GamePlayer.findPlayer(initiator)
            .map(GamePlayer::getMatchData)
            .filter(PlayerMatchData::isInTeam)
            .map(PlayerMatchData::getTeam)
            .ifPresent((team) -> {
              stack.setType(colored.getMaterial(team.getTeamEnum().getDyeColor()));
            }));
    return stack;
  }

  @Override
  protected void handleClick(@NonNull GameMatch match, PlayerInteractEvent event) {
    Player entity = event.getPlayer();
    GamePlayer player = GamePlayer.getPlayer(entity);
    SoundRecord.OPEN_INVENTORY.play(player);
    createInventory(match, player).open(entity);
  }

  @CheckReturnValue
  private SpecialInventory<?> createInventory(@NonNull GameMatch match,
                                              @NonNull GamePlayer player) {
    int maxTeamSize = match.getTeamSize();
    TeamMap teamMap = match.getTeamMap();
    InventoryDimensions dimensions = InventoryDimensions.ofLengths(
        InventoryDimensions.DEFAULT_COLUMN_COUNT,
        1 + ((teamMap.size() - 1) / (InventoryDimensions.DEFAULT_COLUMN_COUNT - 2)));
    InventoryPage page = new InventoryPage(dimensions);
    SpecialInventory<?> inventory = new SpecialInventory<>(
        null, INVENTORY_UPDATE_INTERVAL, menuTitle, page);
    InventoryItem glass = InventoryItem.of(
        ItemBuilder.builder(Material.GRAY_STAINED_GLASS_PANE)
            .name(StringUtils.SPACE)
            .build(),
        (ignored, event) -> event.setCancelled(true));
    page.fill(glass);
    final int width = dimensions.getWidth();
    Iterator<GameTeam> iterator = match.getTeamMap().iterator();
    for (int i = 0; iterator.hasNext(); ++i)
      page.set(InventoryPosition.toIndex(1 + i % (width - 2), (i / (width - 2)), width),
          new TeamItem(match, player, iterator.next(), maxTeamSize, inventory));
    return inventory;
  }

  private final class TeamItem implements InventoryItem {

    private final @NonNull GameMatch match;
    private final @NonNull GamePlayer player;
    private final @NonNull GameTeam team;
    private final int maxTeamSize;
    private final SpecialInventory<?> inventory;

    private final ItemBuilder itemBuilder;

    public TeamItem(
        @NonNull GameMatch match,
        @NonNull GamePlayer player,
        @NonNull GameTeam team,
        @NonNegative int maxTeamSize,
        @NonNull SpecialInventory<?> inventory) {
      this.match = match;
      this.player = player;
      this.team = team;
      this.maxTeamSize = maxTeamSize;
      this.inventory = inventory;
      this.itemBuilder = ItemBuilder
          .builder(REPLACEABLE_MATERIAL.getMaterial(team.getTeamEnum().getDyeColor()))
          .name(team.getTeamEnum().getChatColor().toString() + ChatColor.BOLD
              + team.getTeamEnum().getTranslatedName())
          .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
    }

    public boolean isInTeam() {
      return team.equals(player.getMatchData().getTeam());
    }

    @Override
    public ItemStack get(long ticks) {
      final boolean isTeam = isInTeam();

      List<String> lore = new ArrayList<>();
      if (isTeam) lore.add("§7» " + teamStatusJoined);
      else if (!team.hasSpace()) lore.add("§8» " + teamStatusFull);
      else lore.add((ticks % 2 == 0 ? "§8» " : "   ") + ChatColor.GRAY + teamStatusJoinable);
      List<String> members = team.stream()
          .map(GamePlayer::getName)
          .map((name) -> Language.getInstance().substitute(teamMemberSlot, name))
          .collect(Collectors.toList());
      lore.add(StringUtils.SPACE);
      lore.addAll(members);
      IntStream.range(0, maxTeamSize - members.size())
          .forEach((i) -> lore.add(Language.getInstance().substitute(teamMemberSlot, "(free)")));

      return itemBuilder
          .enchants(isTeam ? Map.of(Enchantment.LUCK, 1) : Map.of())
          .lore(lore)
          .build();
    }

    @Override
    public void click(GamePlayer player, InventoryClickEvent event) {
      final PlayerMatchData data = player.getMatchData();
      event.setCancelled(true);
      if (isInTeam()) return;
      try {
        Preconditions.checkState(team.hasSpace());
        Optional.ofNullable(data.getTeam()).ifPresent((previous) -> {
          Preconditions.checkState(previous.remove(player));
        });
        Preconditions.checkState(team.add(player));
        inventory.updateInventory();
        player.findOnline().ifPresent((p) -> give(match, p));
        SoundRecord.ACTION_SUCCESS.play(player);
      } catch (Exception e) {
        player.sendMessage(Language.getInstance()
            .get(MessageKeys.Match.TEAM_SWITCH_ERROR)
            .substitute(team, ArrayPath.of("team")));
        SoundRecord.ACTION_ERROR.play(player);
      }
    }
  }

}
