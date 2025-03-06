package dev.lyphium.egghunt.listener;

import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.manager.StatisticManager;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public final class PlayerLister implements Listener {

    private final Random random = new Random(System.currentTimeMillis());

    private final ResourceManager resourceManager;
    private final EggManager eggManager;
    private final StatisticManager statisticManager;

    public PlayerLister(
            @NotNull ResourceManager resourceManager,
            @NotNull EggManager eggManager,
            @NotNull StatisticManager statisticManager
    ) {
        this.resourceManager = resourceManager;
        this.eggManager = eggManager;
        this.statisticManager = statisticManager;
    }

    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        eggManager.resetSpawnTimer(player.getUniqueId());
    }

    @EventHandler
    private void onItemPickup(@NotNull EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        final ItemStack item = event.getItem().getItemStack();

        final ItemMeta itemMeta = item.getItemMeta();
        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(NamespacedKeyConstants.NATURAL_EGG_KEY))
            return;

        statisticManager.addPoints(player, 1);
        container.remove(NamespacedKeyConstants.NATURAL_EGG_KEY);
        item.setItemMeta(itemMeta);
    }

    @EventHandler
    private void onInteract(@NotNull PlayerInteractEvent event) {
        final ItemStack item = event.getItem();

        if (item == null || !item.getItemMeta().getPersistentDataContainer().has(NamespacedKeyConstants.EASTER_EGG_KEY))
            return;


        final Player player = event.getPlayer();
        if (event.getAction().isRightClick()) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        if (!event.getAction().isLeftClick() || !player.isSneaking()) {
            return;
        }

        event.setUseItemInHand(Event.Result.DENY);

        final PlayerInventory inventory = player.getInventory();
        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
        player.playSound(resourceManager.getOpenSound());

        final EasterEggDrop drop = resourceManager.getRandomDrop();
        final Location location = player.getLocation();

        if (drop.getItemDrop() != null) {
            final int amount = random.nextInt(drop.getMinimumAmount(), drop.getMaximumAmount() + 1);
            final ItemStack dropItem = drop.getItemDrop().clone();
            dropItem.setAmount(amount);

            final HashMap<Integer, ItemStack> remaining = inventory.addItem(dropItem);

            for (final ItemStack remainingItem : remaining.values()) {
                player.getWorld().dropItemNaturally(location, remainingItem, i -> i.setOwner(player.getUniqueId()));
            }

            return;
        }

        if (drop.getCommandDrop() != null) {
            /*
             * Values to replace
             * @p -> Player opening the egg
             * @r -> Random online player
             * @n -> Nearest other player
             */

            final List<? extends Player> players = Bukkit.getOnlinePlayers().stream().toList();
            final Player random = players.get(this.random.nextInt(players.size()));

            Player target = player;
            if (players.size() > 1) {
                double distanceSquared = Double.MAX_VALUE;
                for (final Player other : players) {
                    if (other == player)
                        continue;

                    double temp = other.getLocation().distanceSquared(location);
                    if (temp < distanceSquared) {
                        target = other;
                        distanceSquared = temp;
                    }
                }
            }

            final String command = drop.getCommandDrop()
                    .replace(" @p ", ' ' + player.getName() + ' ')
                    .replace(" @r ", ' ' + random.getName() + ' ')
                    .replace(" @n ", ' ' + target.getName() + ' ');

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
