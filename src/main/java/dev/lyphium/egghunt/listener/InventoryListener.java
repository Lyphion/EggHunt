package dev.lyphium.egghunt.listener;

import dev.lyphium.egghunt.inventory.DropsInventory;
import dev.lyphium.egghunt.inventory.EasterEggInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class InventoryListener implements Listener {

    @EventHandler
    private void onInventoryClick(@NotNull InventoryClickEvent event) {
        final HumanEntity human = event.getWhoClicked();
        final Inventory inv = event.getInventory();
        final int slot = event.getRawSlot();
        final ItemStack item = event.getCurrentItem();

        if (inv.getHolder() instanceof EasterEggInventory eggInventory) {
            event.setCancelled(true);

            // Check if out of bounds
            if (slot < 0 || item == null)
                return;

            // Check if new item should be added
            if (slot >= inv.getSize()) {
                eggInventory.addEgg(item);
                return;
            }

            // Check if item should be removed
            if (event.getClick().isKeyboardClick()) {
                if (event.getClick() != ClickType.CONTROL_DROP && event.getClick() != ClickType.DROP)
                    return;

                final ItemStack removedItem = eggInventory.removeEgg(slot);
                if (removedItem != null)
                    human.getInventory().addItem(removedItem);

                return;
            }

            // Check if item is requested
            if (event.isLeftClick()) {
                final ItemStack eggItem = eggInventory.getEgg(slot);

                if (eggItem != null)
                    human.getInventory().addItem(eggItem);
                return;
            }

            return;
        }

        if (inv.getHolder() instanceof DropsInventory dropsInventory) {
            event.setCancelled(true);
            return;
        }

    }

}
