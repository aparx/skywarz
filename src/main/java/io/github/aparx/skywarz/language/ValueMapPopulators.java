package io.github.aparx.skywarz.language;

import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.game.team.Team;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 14:33
 * @since 1.0
 */
@UtilityClass
public final class ValueMapPopulators {

  public static void populatePlayer(Map<String, Object> map, Player player, ArrayPath prefix) {
    map.put(prefix.add("name").join(), player.getName());
    map.put(prefix.add("displayName").join(), player.getDisplayName());
    map.put(prefix.add("health").join(), player.getHealth());
    map.put(prefix.add("foodLevel").join(), player.getFoodLevel());
  }

  public static void populateTeam(Map<String, Object> map, Team team, ArrayPath prefix) {
    map.put(prefix.add("name").join(), Language.getInstance().getTeamName(team.getTeamEnum()));
    map.put(prefix.add("color").join(), team.getTeamEnum().getChatColor());
  }

}
