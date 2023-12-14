package io.github.aparx.skywarz.command.commands.kit;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.kit.GameKit;
import io.github.aparx.skywarz.game.kit.GameKitManager;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import io.github.aparx.skywarz.startup.Main;
import io.github.aparx.skywarz.utils.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 17:55
 * @since 1.0
 */
public class KitCreateCommand extends CommandNode {

  public KitCreateCommand(@Nullable CommandNode parent) {
    super(CommandInfo.builder("create")
        .permission(SkywarsPermission.SETUP)
        .args("<DisplayName...>")
        .description("Create a new empty kit")
        .build(), parent);
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    if (args.isEmpty())
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      String displayName = ChatColor.translateAlternateColorCodes('&', args.join());
      String kitName = ChatColor.stripColor(displayName);
      GameKitManager kitManager = Skywars.getInstance().getKitManager();
      Preconditions.checkState(!kitManager.contains(kitName), "Kit already exists");
      kitManager.getKits().add(GameKit.builder(kitName)
          .icon(ItemBuilder.builder()
              .name(ChatColor.RESET + displayName)
              .material(Material.CHEST)
              .wrap())
          .build());
      kitManager.save();
      context.getSender().sendMessage(Language.getInstance().substitute(List.of(
          "{successPrefix} Kit '{2}' was created with the display '{3}§a'.",
          "{successPrefix} Use {0}'/{1} edit {2}'§a to edit the kit!"
      ), ChatColor.GRAY, getParent().createCommand(Main.SHORT_COMMAND), kitName, displayName));
    }
  }
}
