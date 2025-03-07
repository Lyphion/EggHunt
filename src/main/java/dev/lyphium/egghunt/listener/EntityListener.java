package dev.lyphium.egghunt.listener;

import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.manager.StatisticManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;

public final class EntityListener implements Listener {

    private final Random random = new Random(System.currentTimeMillis());

    private final ResourceManager resourceManager;
    private final StatisticManager statisticManager;

    public EntityListener(
            @NotNull ResourceManager resourceManager,
            @NotNull StatisticManager statisticManager
    ) {
        this.resourceManager = resourceManager;
        this.statisticManager = statisticManager;
    }

    @EventHandler
    private void onItemPickup(@NotNull EntityPickupItemEvent event) {
        final ItemStack item = event.getItem().getItemStack();
        final ItemMeta itemMeta = item.getItemMeta();

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(NamespacedKeyConstants.NATURAL_EGG_KEY))
            return;

        if (!(event.getEntity() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }

        final int oldRank = statisticManager.getStatistic(player).getA();

        final int count = statisticManager.addPoints(player, 1);
        container.remove(NamespacedKeyConstants.NATURAL_EGG_KEY);
        item.setItemMeta(itemMeta);

        boolean shootFirework = false;
        if (count % resourceManager.getFireworkFactor() == 0) {
            shootFirework = true;

            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendMessage(TextConstants.PREFIX.append(Component.translatable("announcement.leaderboard.factor", ColorConstants.DEFAULT,
                        p.displayName().color(ColorConstants.HIGHLIGHT), Component.text(count, ColorConstants.ERROR))));
            });
        }

        final int newRank = statisticManager.getStatistic(player).getA();

        if (newRank == 1 && oldRank != newRank) {
            shootFirework = true;

            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendMessage(TextConstants.PREFIX.append(Component.translatable("announcement.leaderboard.change", ColorConstants.DEFAULT,
                        p.displayName().color(ColorConstants.HIGHLIGHT), Component.text(count, ColorConstants.ERROR))));
                p.playSound(resourceManager.getLeaderboardSound());
            });
        }

        if (shootFirework) {
            final FireworkEffect effect = FireworkEffect.builder()
                    .withColor(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.FUCHSIA)
                    .withFade(Color.WHITE)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withFlicker()
                    .withTrail()
                    .build();

            final Firework firework = player.getWorld().spawn(player.getLocation().add(0, 3, 0), Firework.class);
            final FireworkMeta meta = firework.getFireworkMeta();

            meta.addEffect(effect);
            meta.setPower(2);
            firework.setTicksToDetonate(40);
            firework.setFireworkMeta(meta);
        }
    }

    @EventHandler
    private void onInventoryPickup(@NotNull InventoryPickupItemEvent event) {
        final ItemStack item = event.getItem().getItemStack();
        final ItemMeta itemMeta = item.getItemMeta();

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(NamespacedKeyConstants.NATURAL_EGG_KEY))
            return;

        event.setCancelled(true);
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

        if (!event.getAction().isLeftClick() || !player.isSneaking())
            return;

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
            final String formatCommand = EasterEggDrop.getFormatCommand(drop.getCommandDrop(), player);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatCommand);
        }
    }
}
