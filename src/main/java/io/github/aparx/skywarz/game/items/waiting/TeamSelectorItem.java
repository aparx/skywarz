package io.github.aparx.skywarz.game.items.waiting;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.inventory.*;
import io.github.aparx.skywarz.game.items.GameItem;
import io.github.aparx.skywarz.game.match.Match;
import io.github.aparx.skywarz.game.match.MatchState;
import io.github.aparx.skywarz.game.team.Team;
import io.github.aparx.skywarz.game.team.TeamEnum;
import io.github.aparx.skywarz.game.team.TeamMap;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.language.MessageKeys;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.material.ColoredMaterial;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 06:57
 * @since 1.0
 */
public final class TeamSelectorItem extends GameItem {

  private final TickDuration INVENTORY_UPDATE_INTERVAL = TickDuration.of(TimeUnit.TICKS, 10);

  public TeamSelectorItem() {
    super("teamSelector", MatchState.WAITING);
  }

  @Override
  protected ItemStack createItemStack(@NonNull Match match, @NonNull Player initiator) {
    return Skywars.getInstance().getConfigHandler().getItems().getTeamSelector().getStack();
  }

  @Override
  protected void handleClick(@NonNull Match match, PlayerInteractEvent event) {
    Player entity = event.getPlayer();
    SkywarsPlayer player = SkywarsPlayer.getPlayer(entity);
    player.playSound(Sound.BLOCK_LEVER_CLICK, 1.0F, 1.0F);
    createInventory(match, player).open(entity);
  }

  @CheckReturnValue
  private GameInventory createInventory(@NonNull Match match, @NonNull SkywarsPlayer player) {
    int maxTeamSize = match.getArena().getData().getSettings().getTeamSize();
    TeamMap teamMap = match.getTeamMap();
    InventoryDimensions dimensions = InventoryDimensions.ofLengths(
        InventoryDimensions.DEFAULT_COLUMN_COUNT,
        1 + ((teamMap.size() - 1) / InventoryDimensions.DEFAULT_COLUMN_COUNT));
    GameInventory inventory = new GameInventory(null, dimensions, INVENTORY_UPDATE_INTERVAL,
        "§bTeam Selector");
    InventoryContent content = inventory.getContent();
    InventoryItem glass = InventoryItem.of(
        ItemBuilder.builder(Material.GRAY_STAINED_GLASS_PANE)
            .name(StringUtils.SPACE)
            .build(),
        (ignored, event) -> event.setCancelled(true));
    content.fill(glass);
    final int width = dimensions.getWidth();
    Iterator<Team> iterator = match.getTeamMap().iterator();
    for (int cursor = 0; iterator.hasNext(); ++cursor)
      content.set(InventoryPosition.toIndex(
              cursor % width, (cursor / width), width),
          new TeamItem(player, iterator.next(), maxTeamSize, inventory));
    return inventory;
  }

  private static final class TeamItem implements InventoryItem {

    private final @NonNull SkywarsPlayer player;
    private final @NonNull Team team;
    private final int maxTeamSize;
    private final GameInventory inventory;

    private final ItemBuilder itemBuilder;

    public TeamItem(
        @NonNull SkywarsPlayer player,
        @NonNull Team team,
        @NonNegative int maxTeamSize,
        @NonNull GameInventory inventory) {
      this.player = player;
      this.team = team;
      this.maxTeamSize = maxTeamSize;
      this.inventory = inventory;
      this.itemBuilder = ItemBuilder
          .builder(ColoredMaterial.CONCRETE.getMaterial(team.getTeamEnum().getDyeColor()))
          .name(team.getTeamEnum().getChatColor().toString() + ChatColor.BOLD
              + Language.getInstance().getTeamName(team.getTeamEnum()))
          .flags(ItemFlag.HIDE_ENCHANTS);
    }

    public boolean isTeamFull() {
      return team.size() >= maxTeamSize;
    }

    public boolean isInTeam() {
      return team.equals(player.getMatchData().getTeam());
    }

    @Override
    public ItemStack get(long ticks) {
      final boolean isTeam = isInTeam();

      List<String> lore = new ArrayList<>();
      // TODO add translatable
      if (isTeam) lore.add("§7» Joined this team");
      else if (isTeamFull()) lore.add("§8» This team is full");
      else lore.add((ticks % 2 == 0 ? "§8» " : "   ") + "§7Click to join team");
      List<String> members = team.stream()
          .map(SkywarsPlayer::getName)
          .map((name) -> String.format("§8• %s", name))
          .collect(Collectors.toList());
      lore.add(StringUtils.SPACE);
      lore.addAll(members);
      IntStream.range(0, maxTeamSize - members.size())
          .forEach((i) -> lore.add("§8• (free)"));

      return itemBuilder
          .enchants(isTeam ? Map.of(Enchantment.LUCK, 1) : Map.of())
          .lore(lore)
          .build();
    }

    @Override
    public void click(SkywarsPlayer player, InventoryClickEvent event) {
      final PlayerMatchData data = player.getMatchData();
      event.setCancelled(true);
      if (isInTeam()) return;
      try {
        Preconditions.checkState(!isTeamFull());
        Optional.ofNullable(data.getTeam()).ifPresent((previous) -> {
          Preconditions.checkState(previous.remove(player));
        });
        Preconditions.checkState(team.add(player));
        player.sendMessage((language) -> language
            .get(MessageKeys.Match.TEAM_SWITCH_SUCCESS)
            .substitute(team, ArrayPath.of("team")));
        inventory.updateInventory();
        player.playSound(Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
      } catch (Exception e) {
        player.sendMessage((language) -> language
            .get(MessageKeys.Match.TEAM_SWITCH_ERROR)
            .substitute(team, ArrayPath.of("team")));
        player.playSound(Sound.BLOCK_ANVIL_LAND, .33f, .75f);
      }
    }
  }

}
