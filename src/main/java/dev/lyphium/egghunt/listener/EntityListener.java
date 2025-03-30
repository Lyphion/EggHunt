package dev.lyphium.egghunt.listener;

import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.manager.StatisticManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import dev.lyphium.egghunt.util.TextConstants;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.sound.Sound;
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

        // Check if item was an egg
        final PersistentDataContainerView container = item.getPersistentDataContainer();
        if (!container.has(NamespacedKeyConstants.NATURAL_EGG_KEY))
            return;

        // Only allow players to pick it up
        if (!(event.getEntity() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }

        // Handle fake egg
        if (container.has(NamespacedKeyConstants.FAKE_EGG_KEY)) {
            event.getItem().setItemStack(ItemStack.empty());

            final Location location = event.getItem().getLocation();
            final World world = location.getWorld();
            world.spawnParticle(Particle.EXPLOSION, location, 10);
            world.playSound(Sound.sound(NamespacedKey.minecraft("entity.generic.explode"), Sound.Source.NEUTRAL, 1.0f, 1.0f));
            player.damage(5);
            return;
        }

        // Get old rank of the player to compare it afterward.
        final int oldRank = statisticManager.getStatistic(player.getUniqueId()).getA();

        // Update statistic
        final int count = statisticManager.addPoints(player.getUniqueId(), 1);

        // Remove tag, which indicate a new egg
        item.editPersistentDataContainer(c -> c.remove(NamespacedKeyConstants.NATURAL_EGG_KEY));

        boolean shootFirework = false;

        // Check if egg count crossed milestone
        if (count % resourceManager.getMilestone() == 0) {
            shootFirework = true;

            // Notify all player
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(TextConstants.PREFIX.append(Component.translatable("announcement.leaderboard.factor", ColorConstants.DEFAULT,
                    player.displayName().color(ColorConstants.HIGHLIGHT), Component.text(count, ColorConstants.ERROR)))));
        }

        // Check if player reached rank 1
        final int newRank = statisticManager.getStatistic(player.getUniqueId()).getA();
        if (newRank == 1 && oldRank != newRank) {
            shootFirework = true;

            // Notify all player
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendMessage(TextConstants.PREFIX.append(Component.translatable("announcement.leaderboard.change", ColorConstants.DEFAULT,
                        player.displayName().color(ColorConstants.HIGHLIGHT), Component.text(count, ColorConstants.ERROR))));
                p.playSound(resourceManager.getLeaderboardSound());
            });
        }

        // Check if firework should be spawned (milestone or new first place)
        if (!shootFirework)
            return;

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

    @EventHandler
    private void onInventoryPickup(@NotNull InventoryPickupItemEvent event) {
        final ItemStack item = event.getItem().getItemStack();

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
