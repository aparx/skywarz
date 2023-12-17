package io.github.aparx.skywarz.command.commands.arena.update;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.CommandInfo;
import io.github.aparx.skywarz.command.arguments.CommandArgList;
import io.github.aparx.skywarz.command.commands.arena.AbstractArenaCommand;
import io.github.aparx.skywarz.command.skeleton.CommandNode;
import io.github.aparx.skywarz.game.arena.ArenaData;
import io.github.aparx.skywarz.game.arena.GameArena;
import io.github.aparx.skywarz.game.arena.settings.ArenaRule;
import io.github.aparx.skywarz.language.Language;
import io.github.aparx.skywarz.permission.SkywarsPermission;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-17 16:12
 * @since 1.0
 */
public class ArenaSetRuleCommand extends CommandNode {

  public ArenaSetRuleCommand(@Nullable CommandNode parent) {
    super(CommandInfo.builder("rule")
            .permission(SkywarsPermission.SETUP)
            .args("<{children}> <Arena> <Value>")
            .build(),
        parent);
    for (ArenaRule rule : ArenaRule.values())
      add(new RuleCommand(rule));
  }

  @Override
  public void execute(CommandContext context, CommandArgList args) {
    context.setStatus(CommandContext.Status.ERROR_SYNTAX);
  }

  private final class RuleCommand extends AbstractArenaCommand {

    private final @NonNull ArenaRule rule;

    public RuleCommand(@NonNull ArenaRule rule) {
      super(CommandInfo.builder(rule.getRule().getName())
              .aliases(rule.name().toLowerCase())
              .usage("<Arena> <Value>")
              .build(),
          0, ArenaSetRuleCommand.this);
      this.rule = rule;
    }

    @Override
    protected void execute(GameArena arena, CommandContext context, CommandArgList args) {
      if (args.length() != 2)
        context.setStatus(CommandContext.Status.ERROR_SYNTAX);
      else {
        ArenaData data = arena.getData();
        data.setSettings(data.getSettings().withRule(rule, args.getString(1)));
        context.getSender().sendMessage(Language.getInstance().substitute(
            "{successPrefix} Updated rule '{0}' for '{1}' to '{2}' (unsaved)",
            rule.getRule().getName(), arena.getName(), data.getSettings().getRuleValue(rule)
        ));
      }
    }

    @Override
    public List<String> onTabComplete(CommandContext context, CommandArgList args) {
      if (args.length() < 2)
        return super.onTabComplete(context, args);
      return rule.getRule().getSuggestions();
    }
  }
}
