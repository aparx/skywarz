package io.github.aparx.skywarz.utils;

import io.github.aparx.skywarz.command.CommandContext;
import io.github.aparx.skywarz.command.tree.CommandNode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-02 07:06
 * @since 1.0
 */
@UtilityClass
public final class PaginationUtils {

  public static int getPageAmount(int elementCount, int maxPerPage) {
    return (int) Math.ceil(elementCount / (double) maxPerPage);
  }

  public static <T> Stream<T> paginate(Collection<T> collection, int page, int maxPerPage) {
    return paginate(collection.stream(), page, maxPerPage);
  }

  public static <T> Stream<T> paginate(Stream<T> stream, int page, int maxPerPage) {
    return stream.skip((long) (page - 1) * maxPerPage).limit(maxPerPage);
  }

  @Getter
  @Builder
  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public static final class CommandChatPaginator<E> {

    private static final String SKIP_BUTTON = "[»]";

    private final @NonNegative int maxPerPage;
    private final @NonNull CommandNode node;
    private final @NonNull String title;
    private final @NonNull BiFunction<CommandContext, Stream<E>, String> contentFactory;
    private final @NonNull BiFunction<CommandContext, Integer, String> nextPageCommandFactory;

    public void sendPage(Collection<E> elements, CommandContext context, int page) {
      final int maxPages = getPageAmount(elements.size(), maxPerPage);
      page = Math.max(Math.min(page, maxPages), 1);

      StringBuilder builder = new StringBuilder();
      String padding = "=".repeat(10);
      String header = String.format("%s %s %s of %s %s",
          padding, title, page, maxPages, padding);
      builder.append(ChatColor.DARK_GRAY).append(header).append('\n');
      Stream<E> paginate = PaginationUtils.paginate(elements.stream(), page, maxPerPage);
      String content = contentFactory.apply(context, paginate);
      builder.append(content);
      if (context.isPlayer() && page < maxPages) {
        Player player = context.getPlayer();
        player.sendMessage(builder.toString());

        String filler = "=".repeat((header.length() - SKIP_BUTTON.length() - 2) / 2);

        // leading padding
        TextComponent component = new TextComponent(filler + ' ');
        component.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
        // clickable component
        TextComponent clickable = new TextComponent(SKIP_BUTTON);
        clickable.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
        clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
            nextPageCommandFactory.apply(context, 1 + page)));
        clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            TextComponent.fromLegacyText("Go to the next page")));
        component.addExtra(clickable);
        // tailing padding
        TextComponent extraFiller = new TextComponent(' ' + filler);
        extraFiller.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
        component.addExtra(extraFiller);
        player.spigot().sendMessage(component);
      } else {
        builder.append(ChatColor.DARK_GRAY).append("=".repeat(header.length()));
        context.getSender().sendMessage(builder.toString());
      }
    }
  }

}
