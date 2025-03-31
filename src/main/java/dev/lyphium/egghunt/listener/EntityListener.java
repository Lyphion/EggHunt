package dev.lyphium.egghunt.listener;

import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;

public final class EntityListener implements Listener {

    private final Random random = new Random(System.currentTimeMillis());

    private final ResourceManager resourceManager;
    private final EggManager eggManager;

    public EntityListener(
            @NotNull ResourceManager resourceManager,
            @NotNull EggManager eggManager
    ) {
        this.resourceManager = resourceManager;
        this.eggManager = eggManager;
    }

    @EventHandler
    private void onItemPickup(@NotNull EntityPickupItemEvent event) {
        final Item item = event.getItem();
        final PersistentDataContainerView container = item.getPersistentDataContainer();

        // Check if item was an egg
        if (!container.has(NamespacedKeyConstants.NATURAL_EGG_KEY))
            return;

        // Only allow players to pick it up
        if (!(event.getEntity() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }

        final boolean fake = container.has(NamespacedKeyConstants.FAKE_EGG_KEY);
        final boolean breakable = container.has(NamespacedKeyConstants.BREAKABLE_EGG_KEY);

        boolean success = eggManager.handlePickup(player, item.getLocation(), fake, breakable);
        if (!success) {
            item.setItemStack(ItemStack.empty());
        }
    }

    @EventHandler
    private void onInventoryPickup(@NotNull InventoryPickupItemEvent event) {
        final Item item = event.getItem();

        // Only player should be able to pick up egg
        final PersistentDataContainerView container = item.getPersistentDataContainer();
        if (!container.has(NamespacedKeyConstants.NATURAL_EGG_KEY))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    private void onInteract(@NotNull PlayerInteractEvent event) {
        final ItemStack item = event.getItem();

        // We only care for eggs
        if (item == null || !item.getPersistentDataContainer().has(NamespacedKeyConstants.EASTER_EGG_KEY))
            return;

        // Only left-click+shift is relevant, everything else should not work
        final Player player = event.getPlayer();
        if (event.getAction().isRightClick()) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        if (!event.getAction().isLeftClick() || !player.isSneaking())
            return;

        event.setUseItemInHand(Event.Result.DENY);

        // Remove 1 egg from inventory
        final PlayerInventory inventory = player.getInventory();
        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
        // Player sound indicating usage
        player.playSound(resourceManager.getOpenSound());

        // Get drop of the egg
        final EasterEggDrop drop = resourceManager.getRandomDrop();
        final Location location = player.getLocation();

        // Handle item drop
        if (drop.getItemDrop() != null) {
            // Create and give item drop
            final int amount = random.nextInt(drop.getMinimumAmount(), drop.getMaximumAmount() + 1);
            final ItemStack dropItem = drop.getItemDrop().clone();
            dropItem.setAmount(amount);

            // If player has no space in the inventory, drop it
            final HashMap<Integer, ItemStack> remaining = inventory.addItem(dropItem);
            for (final ItemStack remainingItem : remaining.values()) {
                player.getWorld().dropItemNaturally(location, remainingItem, i -> i.setOwner(player.getUniqueId()));
            }

            return;
        }

        // Handle command drop
        if (drop.getCommandDrop() != null) {
            // Format command, replace placeholder
            final String formatCommand = EasterEggDrop.getFormatedCommand(drop.getCommandDrop(), player);

            // Run command as console
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatCommand);
        }
    }
}
