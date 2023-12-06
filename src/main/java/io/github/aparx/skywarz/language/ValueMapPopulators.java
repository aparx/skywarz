package io.github.aparx.skywarz.language;

import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.team.Team;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 14:33
 * @since 1.0
 */
@UtilityClass
public final class ValueMapPopulators {

  public static void populatePlayer(Map<String, Object> map, Player entity, ArrayPath prefix) {
    map.put(prefix.add("name").join(), entity.getName());
    map.put(prefix.add("displayName").join(), entity.getDisplayName());
    map.put(prefix.add("health").join(), entity.getHealth());
    map.put(prefix.add("foodLevel").join(), entity.getFoodLevel());
    SkywarsPlayer.findPlayer(entity)
        .flatMap(player -> player.getPlayerData()
            .find(PlayerMatchData.class)
            .map(PlayerMatchData::getTeam))
        .ifPresent((team) -> populateTeam(map, team, prefix.add("team")));
  }

  public static void populateTeam(Map<String, Object> map, Team team, ArrayPath prefix) {
    map.put(prefix.add("name").join(), Language.getInstance().getTeamName(team.getTeamEnum()));
    map.put(prefix.add("color").join(), team.getTeamEnum().getChatColor());
  }

}
