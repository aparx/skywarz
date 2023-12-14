package io.github.aparx.skywarz.bungeecord;

import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.events.match.MatchCreateEvent;
import io.github.aparx.skywarz.events.match.MatchLeaveEvent;
import io.github.aparx.skywarz.events.match.phase.MatchPhaseStopEvent;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.game.match.GameMatch;
import io.github.aparx.skywarz.game.match.GameMatchState;
import io.github.aparx.skywarz.handler.DefaultSkywarsHandler;
import io.github.aparx.skywarz.handler.MainConfig;
import io.github.aparx.skywarz.language.*;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import io.github.aparx.skywarz.utils.tick.TickDuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A listener used to register events that may be required for the "Bungeecord" option.
 *
 * @author aparx (Vinzent Z.)
 * @version 2023-12-11 22:03
 * @since 1.0
 */
public class BungeeListener implements Listener {

  public final boolean isBungeecord() {
    return MainConfig.getInstance().isBungeeEnabled();
  }

  public final GameArena getBungeeArena() {
    return findBungeeArena().orElseThrow(() -> {
      return new IllegalStateException("Default arena could not be found");
    });
  }

  public final Optional<GameArena> findBungeeArena() {
    return Skywars.getInstance().getArenaManager().find(
        MainConfig.getInstance().getBungeeArena());
  }

  @SuppressWarnings("DataFlowIssue") // OK, IDE static analysis bug
  @EventHandler(priority = EventPriority.LOW)
  void onQuit(PlayerQuitEvent event) {
    if (isBungeecord())
      event.setQuitMessage(null);
  }

  @SuppressWarnings("DataFlowIssue") // OK, IDE static analysis bug
  @EventHandler(priority = EventPriority.LOW)
  void onJoin(PlayerJoinEvent event) {
    if (!isBungeecord()) return;
    event.setJoinMessage(null);
    Player player = event.getPlayer();
    try {
      Skywars.getInstance().getMatchManager().join(
          SkywarsPlayer.getPlayer(player), getBungeeArena());
    } catch (Exception e) {
      Skywars.logger().log(Level.FINE, "Player could not join (although required)", e);
      String errorMessage = ChatColor.RED + (!(e instanceof LocalizableError)
          ? Language.getInstance().substitute(MessageKeys.Match.JOIN_ERROR)
          : e.getLocalizedMessage());
      if (SkywarsPermission.SETUP.has(player)) {
        player.sendMessage(errorMessage);
        player.sendMessage(ChatColor.RED + "Reason: " + e.getMessage());
      } else Skywars.getInstance().getBungeeHandler().sendToFallback(player);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onLeave(MatchLeaveEvent event) {
    if (isBungeecord())
      event.getPlayer().findOnline().ifPresent((online) -> {
        Skywars.getInstance().getBungeeHandler().sendToFallback(online);
      });
  }

  @EventHandler(priority = EventPriority.LOW)
  void onCreate(MatchCreateEvent event) {
    if (isBungeecord() && Skywars.getInstance().getMatchManager().size() > 1) {
      event.setCancelled(true);
      throw new IllegalArgumentException(
          "There cannot be more than one match simultaneously with bungeecord mode enabled");
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onEnd(MatchPhaseStopEvent event) {
    if (isBungeecord() && GameMatchState.DONE.equals(event.getPhase().getState())) {
      if (Skywars.plugin().isEnabled())
        Bukkit.getScheduler().runTaskLater(Skywars.plugin(), () -> {
          // delayed shutdown, so players are sent to fallback servers first
          Bukkit.getServer().shutdown();
        }, TickDuration.ofSecond().toTicks());
      Skywars.logger().log(Level.INFO,
          "Match {0} completed, shutting down server soon (...)",
          Optional.ofNullable(event.getMatch()).map(GameMatch::getId));
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  void onEnd(ServerListPingEvent event) {
    if (isBungeecord()) try {
      Iterator<GameMatch> iterator = Skywars.getInstance().getMatchManager().iterator();
      GameArena bungeeArena = getBungeeArena();
      LazyVariableLookup lookup = new LazyVariableLookup();
      VariablePopulator.addArenaOrAcquiree(lookup, bungeeArena, ArrayPath.of());
      if (iterator.hasNext()) {
        GameMatch match = iterator.next();
        VariablePopulator.addMatch(lookup, match, ArrayPath.of());
        event.setMaxPlayers(match.getMaxPlayerCount());
      } else
        event.setMaxPlayers(GameArena.getMaxPlayerCount(
            bungeeArena.getData().getSettings(),
            GameArena.getAvailableTeamCount(bungeeArena)));
      event.setMotd(MainConfig.getInstance().getBungeeMotd().stream().limit(2)
          .map((line) -> Language.getInstance().substitute(line, lookup))
          .collect(Collectors.joining("\n")));
    } catch (Exception ignored) {}
  }
}
