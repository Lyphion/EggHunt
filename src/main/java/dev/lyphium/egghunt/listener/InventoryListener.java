package dev.lyphium.egghunt.listener;

import dev.lyphium.egghunt.inventory.DropsInventory;
import dev.lyphium.egghunt.inventory.EasterEggInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public final class InventoryListener implements Listener {

    @EventHandler
    private void onInventoryClick(@NotNull InventoryClickEvent event) {
        final HumanEntity human = event.getWhoClicked();
        final Inventory inventory = event.getInventory();

        if (inventory.getHolder() instanceof EasterEggInventory eggInventory) {
            event.setCancelled(true);
            return;
        }

        if (inventory.getHolder() instanceof DropsInventory dropsInventory) {
            event.setCancelled(true);
            return;
        }

    }

}
