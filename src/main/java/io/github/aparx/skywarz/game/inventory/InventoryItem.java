package io.github.aparx.skywarz.game.inventory;

import com.google.common.base.Preconditions;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import io.github.aparx.skywarz.utils.item.WrappedItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.LongFunction;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 13:04
 * @since 1.0
 */
public interface InventoryItem extends ItemClickAction {

  ItemStack get(long ticks);

  static InventoryItem of(@NonNull WrappedItemStack stack, @Nullable ItemClickAction action) {
    Preconditions.checkNotNull(stack, "Stack must not be null");
    return of((ticks) -> stack.getStack(), action);
  }

  static InventoryItem of(@NonNull WrappedItemStack stack) {
    return of(stack, null);
  }

  static InventoryItem of(@NonNull ItemStack stack, @Nullable ItemClickAction action) {
    Preconditions.checkNotNull(stack, "Stack must not be null");
    return of((ticks) -> stack, action);
  }

  static InventoryItem of(@NonNull ItemStack stack) {
    return of(stack, null);
  }

  static InventoryItem of(
      @NonNull LongFunction<ItemStack> factory,
      @Nullable ItemClickAction action) {
    Preconditions.checkNotNull(factory, "Factory must not be null");
    return new InventoryItem() {
      public ItemStack get(long ticks) {return factory.apply(ticks);}

      public void click(SkywarsPlayer player, InventoryClickEvent event) {
        if (action != null) action.click(player, event);
      }
    };
  }

  static InventoryItem of(@NonNull LongFunction<ItemStack> factory) {
    return of(factory, null);
  }

}
