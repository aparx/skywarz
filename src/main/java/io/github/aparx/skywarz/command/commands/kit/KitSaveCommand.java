package io.github.aparx.skywarz.command.commands.kit;

import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.game.kit.GameKit;
import io.github.aparx.skywarz.game.kit.GameKitManager;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import io.github.aparx.skywarz.utils.sound.SoundRecord;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 17:55
 * @since 1.0
 */
public class KitSaveCommand extends CommandNode {

  public KitSaveCommand(@Nullable CommandNode parent) {
    super(CommandInfo.builder("save")
        .permission(SkywarsPermission.SETUP)
        .description("Save the kit currently being edited")
        .build(), parent);
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    if (!args.isEmpty())
      context.setStatus(CommandContext.Status.ERROR_SYNTAX);
    else {
      Player entity = context.getPlayer();
      PlayerKitEditMode modify = SkywarsPlayer.findPlayer(entity)
          .map(SkywarsPlayer::getPlayerData)
          .flatMap((data) -> data.find(PlayerKitEditMode.class))
          .filter(PlayerKitEditMode::isInMode)
          .orElseThrow(() -> new IllegalStateException("Not in edit mode"));
      GameKit kit = modify.getKit();
      GameKitManager kitManager = Skywars.getInstance().getKitManager();
      ItemStack[] contents = entity.getInventory().getStorageContents();
      ItemStack[] armor = entity.getInventory().getArmorContents();
      WrappedItemStack icon = kit.getIcon();
      GameKit.KitBuilder kitBuilder = GameKit.builder(kit.getName())
          .icon(icon != null ? icon.copy() : null);

      kitBuilder.fill(Arrays.stream(contents)
          .map((x) -> x == null ? null : WrappedItemStack.parse(x))
          .toArray(WrappedItemStack[]::new));

      for (GameKit.ArmorSlot slot : GameKit.ArmorSlot.values())
        kitBuilder.slot(slot, WrappedItemStack.parse(armor[slot.ordinal()]));

      GameKit rebuilt = kitBuilder.build(); // new to be replacing kit instance
      kitManager.getKits().remove(kit);
      kitManager.getKits().add(rebuilt);
      kitManager.save();
      modify.leave();
      SoundRecord.KIT_BUILT.play(entity);
      entity.sendMessage(Language.getInstance().substitute(
          "{successPrefix} Kit '{0}' was saved successfully!",
          rebuilt.getName()));
    }
  }
}
