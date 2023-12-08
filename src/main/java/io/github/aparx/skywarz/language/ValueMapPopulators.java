package io.github.aparx.skywarz.language;

import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.entity.data.types.PlayerMatchData;
import io.github.aparx.skywarz.game.kit.Kit;
import io.github.aparx.skywarz.game.team.Team;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
        .map(SkywarsPlayer::getPlayerData)
        .flatMap((storage) -> storage.find(PlayerMatchData.class))
        .ifPresent((matchData) -> {
          Kit kit = matchData.getKit();
          if (kit != null)
            populateKit(map, kit, prefix.add("kit"));
          Team team = matchData.getTeam();
          if (team != null)
            populateTeam(map, team, prefix.add("team"));
        });
  }

  public static void populateTeam(Map<String, Object> map, Team team, ArrayPath prefix) {
    map.put(prefix.add("name").join(), Language.getInstance().getTeamName(team.getTeamEnum()));
    map.put(prefix.add("color").join(), team.getTeamEnum().getChatColor());
  }

  public static void populateKit(Map<String, Object> map, Kit kit, ArrayPath prefix) {
    map.put(prefix.add("name").join(), kit.getName());
    map.put(prefix.add("displayName").join(), kit.getDisplayName());
  }

}
