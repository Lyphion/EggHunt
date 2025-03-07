package dev.lyphium.egghunt.listener;

import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.inventory.DropsInventory;
import dev.lyphium.egghunt.inventory.EasterEggInventory;
import dev.lyphium.egghunt.util.OneOf;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class InventoryListener implements Listener {

    @EventHandler
    private void onInventoryClick(@NotNull InventoryClickEvent event) {
        final HumanEntity human = event.getWhoClicked();
        final Inventory inv = event.getInventory();
        final int slot = event.getRawSlot();
        final ItemStack item = event.getCurrentItem();

        // Check for eggs overview inventory
        if (inv.getHolder() instanceof EasterEggInventory eggInventory) {
            event.setCancelled(true);

            // Check if out of bounds
            if (slot < 0 || item == null)
                return;

            // Check for paging
            if (slot == 45 || slot == 53) {
                eggInventory.changePage(slot == 45 ? -1 : 1);
                return;
            }

            // Check if new item should be added
            if (slot >= inv.getSize()) {
                eggInventory.addEgg(item);
                return;
            }

            // Check if item should be removed
            if (event.getClick().isKeyboardClick()) {
                // Only allow "drop"
                if (event.getClick() != ClickType.CONTROL_DROP && event.getClick() != ClickType.DROP)
                    return;

                // Remove egg from pool and give item to player
                final ItemStack removedItem = eggInventory.removeEgg(slot);
                if (removedItem != null)
                    human.getInventory().addItem(removedItem);

                return;
            }

            // Check if item is requested
            if (event.isLeftClick()) {
                // Get egg from pool and give item to player
                final ItemStack eggItem = eggInventory.getEgg(slot);

                if (eggItem != null)
                    human.getInventory().addItem(eggItem);
                return;
            }

            return;
        }

        // Check for drops overview inventory
        if (inv.getHolder() instanceof DropsInventory dropsInventory) {
            event.setCancelled(true);

            // Check if out of bounds
            if (slot < 0 || item == null)
                return;

            // Check for paging
            if (slot == 45 || slot == 53) {
                dropsInventory.changePage(slot == 45 ? -1 : 1);
                return;
            }

            // Ignore click in main inventory
            if (slot >= inv.getSize())
                return;

            // Check if item should be removed
            if (event.getClick().isKeyboardClick()) {
                // Only allow "drop"
                if (event.getClick() != ClickType.CONTROL_DROP && event.getClick() != ClickType.DROP)
                    return;

                // Remove drop from pool and give item to player
                final ItemStack removedItem = dropsInventory.removeDrop(slot);
                if (removedItem != null)
                    human.getInventory().addItem(removedItem);

                return;
            }

            // Check if item is requested
            if (event.isLeftClick()) {
                // Get drop from pool
                final OneOf<ItemStack, String> drop = dropsInventory.getDrop(slot);

                if (drop == null)
                    return;

                // If drop is item, give it to the player
                if (drop.getFirst() != null) {
                    human.getInventory().addItem(drop.getFirst());
                }
                // Otherwise format command and run as console
                else if (human instanceof Player player) {
                    final String formatCommand = EasterEggDrop.getFormatCommand(Objects.requireNonNull(drop.getSecond()), player);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatCommand);
                }
            }
        }
    }
}
