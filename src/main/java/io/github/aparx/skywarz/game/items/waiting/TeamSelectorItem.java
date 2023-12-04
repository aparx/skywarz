package io.github.aparx.skywarz.game.items.waiting;

import com.google.common.base.Preconditions;
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
import io.github.aparx.skywarz.handler.configs.Language;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import io.github.aparx.skywarz.utils.tick.TimeUnit;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 06:57
 * @since 1.0
 */
public final class TeamSelectorItem extends GameItem {

  public TeamSelectorItem() {
    super(MatchState.WAITING);
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
    PlayerMatchData data = player.getMatchData();
    GameInventory inventory = new GameInventory(null, InventoryDimensions.CHEST,
        TickDuration.of(TimeUnit.TICKS, 10), "§bTeam Selector");
    InventoryContent content = inventory.getContent();
    int cursor = 0;
    int maxTeamSize = match.getArena().getData().getSettings().getTeamSize();
    Language language = Language.getLanguage();
    for (Team team : match.getTeamMap()) {
      TeamEnum teamEnum = team.getTeamEnum();
      content.set((++cursor % 9) - 1, InventoryItem.of(
          (ticks) -> {
            boolean isTeam = team.equals(data.getTeam());

            List<String> lore = new ArrayList<>();
            if (isTeam) lore.add("§7» Member of team");
            else lore.add((ticks % 2 == 0 ? "§8» " : "   ") + "§7Click to join team");
            List<String> members = team.stream()
                .map(SkywarsPlayer::getName)
                .map((name) -> String.format("§8• %s", name))
                .collect(Collectors.toList());
            lore.add(StringUtils.SPACE);
            lore.addAll(members);
            IntStream.range(0, maxTeamSize - members.size())
                .forEach((i) -> lore.add("§8• (free)"));

            return ItemBuilder.builder(teamEnum.getMaterial())
                .name(teamEnum.getColor().toString()
                    + ChatColor.BOLD
                    + language.getTeamName(teamEnum))
                .enchants(isTeam ? Map.of(Enchantment.LUCK, 1) : Map.of())
                .lore(lore)
                .flags(ItemFlag.HIDE_ENCHANTS)
                .build();
          },
          (viewer, clickEvent) -> {
            clickEvent.setCancelled(true);
            if (!team.equals(data.getTeam())) {
              Optional.ofNullable(data.getTeam())
                  .ifPresent((x) -> x.remove(player));
              team.add(player);
              viewer.sendMessage("§eSwitched team to " + teamEnum.getColor() + language.getTeamName(teamEnum));
              inventory.updateInventory();
              //inventory.close(viewer.getOnline());
              viewer.playSound(Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
            }
          }));
    }
    inventory.open(entity);
  }

}
