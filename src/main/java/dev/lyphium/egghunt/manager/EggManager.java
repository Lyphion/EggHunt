package dev.lyphium.egghunt.manager;

import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class EggManager {

    public static final Component USAGE_DESCRIPTION = Component.translatable("tutorial.socialInteractions.description", ColorConstants.DEFAULT,
                    Component.keybind("key.sneak").append(Component.text("+")).append(Component.keybind("key.mouse.left")))
            .decoration(TextDecoration.ITALIC, false);

    private final JavaPlugin plugin;
    private final ResourceManager resourceManager;

    /**
     * Collection of next valid time (cooldown) to spawn an egg for a player.
     */
    private final Map<UUID, Long> nextSpawns = new HashMap<>();

    /**
     * Collection of items to remove when hitting the ground.
     */
    private final List<Entity> rainItems = new ArrayList<>();

    private final Random random = new Random(System.currentTimeMillis());

    /**
     * Whether random eggs will spawn.
     */
    @Getter
    private boolean active;

    /**
     * Collection of players for whom no eggs will spawn, or notifications are displayed.
     */
    @Getter
    private final Set<UUID> blacklist = new HashSet<>();

    public EggManager(@NotNull JavaPlugin plugin, @NotNull ResourceManager resourceManager) {
        this.plugin = plugin;
        this.resourceManager = resourceManager;

        Bukkit.getScheduler().runTaskTimer(plugin, this::handleUpdate, 20, 20);
        Bukkit.getScheduler().runTaskTimer(plugin, this::handleRainingItems, 1, 1);
        setActive(true);
    }

    /**
     * Set if random eggs will spawn.
     *
     * @param active State of the spawning.
     */
    public void setActive(boolean active) {
        nextSpawns.clear();
        this.active = active;
    }

    /**
     * Spawn a new egg around a position.
     *
     * @param location Center of the spawning area
     * @return {@code true} if egg was spawned
     */
    public boolean spawn(@NotNull Location location) {
        return spawn(location, false);
    }

    /**
     * Spawn a new egg around a position.
     *
     * @param location Center of the spawning area
     * @param fake     Whether the egg is fake
     * @return {@code true} if egg was spawned
     */
    public boolean spawn(@NotNull Location location, boolean fake) {
        // Get spawning space, if none was found to nothing
        final Location spawn = findSpawnLocation(location);
        if (spawn == null)
            return false;

        final ItemStack item = createItemStack();
        spawnEggEntity(spawn, item, fake, false);

        // Notify players around
        for (final Player player : spawn.getWorld().getNearbyPlayers(spawn, resourceManager.getMaximumRange())) {
            // Skip blacklisted players
            if (blacklist.contains(player.getUniqueId()) && !fake)
                continue;

            player.playSound(resourceManager.getSpawnSound());
            final String format = Objects.requireNonNull(GlobalTranslator.translator().translate("spawn.egg", player.locale())).format(null);
            player.sendActionBar(MiniMessage.miniMessage().deserialize(format));
        }

        return true;
    }

    public void rain(@NotNull Location location) {
        final Location center = location.clone().add(0, resourceManager.getRainOffset(), 0);
        final World world = location.getWorld();
        final int radius = resourceManager.getRainRadius();

        new BukkitRunnable() {
            static final int delay = 5;
            int count = -20;

            @Override
            public void run() {
                if (count >= resourceManager.getRainAmount() * delay) {
                    cancel();
                    return;
                }

                world.spawnParticle(Particle.CLOUD, center, 5 * radius * radius, radius, 0, radius, 0);

                count++;
                if (count <= 0 || count % delay != 0)
                    return;

                final double r = radius * Math.sqrt(random.nextDouble());
                final double theta = random.nextDouble() * Math.PI * 2;

                final Location spawn = center.clone().add(r * Math.cos(theta), 0, r * Math.sin(theta));
                final boolean breakable = random.nextDouble() < resourceManager.getRainBreakProbability();

                // Create Easter egg
                final ItemStack item = createItemStack();
                final Entity entity = spawnEggEntity(spawn, item, false, breakable);

                // Add entity to raining list
                rainItems.add(entity);
            }
        }.runTaskTimer(plugin, 2, 2);
    }

    /**
     * Reset cooldown time, when a new egg spawns around player.
     *
     * @param uuid Player to reset
     */
    public void resetSpawnTimer(@NotNull UUID uuid) {
        final int minimumDuration = resourceManager.getMinimumDuration();
        final int maximumDuration = resourceManager.getMaximumDuration();

        // Find random time in duration range
        long duration = random.nextLong(minimumDuration, maximumDuration + 1);
        nextSpawns.put(uuid, System.currentTimeMillis() + duration * 1000);
    }

    @SuppressWarnings("UnstableApiUsage")
    private @NotNull ItemStack createItemStack() {
        // Get random egg to spawn
        final ItemStack item = resourceManager.getRandomEgg();
        item.editPersistentDataContainer(container -> {
            // Add tags identifying the egg
            container.set(NamespacedKeyConstants.EASTER_EGG_KEY, PersistentDataType.BOOLEAN, true);
        });

        final List<Component> lore = item.hasData(DataComponentTypes.LORE)
                ? new ArrayList<>(Objects.requireNonNull(item.getData(DataComponentTypes.LORE)).lines())
                : new ArrayList<>();

        // Failsafe to only include description once
        if (!lore.contains(USAGE_DESCRIPTION)) {
            // Add usage description
            if (!lore.isEmpty())
                lore.add(Component.empty());

            lore.add(USAGE_DESCRIPTION);
            item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
        }
        return item;
    }

    private Entity spawnEggEntity(@NotNull Location location, @NotNull ItemStack item, boolean fake, boolean breakable) {
        final World world = location.getWorld();

        // Spawn egg in the world
        final Entity entity = switch (resourceManager.getEntityMode()) {
            case ITEM -> world.spawn(location, Item.class, i -> {
                // Set properties
                i.setVelocity(new Vector());
                i.setItemStack(item);
                i.setCanMobPickup(false);
                i.setCanPlayerPickup(true);

                // Workaround to modify lifetime of item
                final ItemEntity itemEntity = ((CraftItem) i).getHandle();
                @SuppressWarnings("resource") final Level level = itemEntity.level();
                final int despawnRate = level.paperConfig().entities.spawning.altItemDespawnRate.enabled
                        ? level.paperConfig().entities.spawning.altItemDespawnRate.items.getOrDefault(itemEntity.getItem(), level.spigotConfig.itemDespawnRate)
                        : level.spigotConfig.itemDespawnRate;

                itemEntity.age = despawnRate - resourceManager.getLifetime() * 20;
            });
            case ARMOR_STAND -> world.spawn(location, ArmorStand.class, a -> {
                // Set properties
                a.setVelocity(new Vector());
                a.setItem(EquipmentSlot.HEAD, item);
                a.setInvisible(true);
                a.setInvulnerable(true);
                a.setMarker(true);
                a.setGravity(false);
                a.setDisabledSlots(EquipmentSlot.values());
            });
        };

        final PersistentDataContainer container = entity.getPersistentDataContainer();

        // Mark egg as natural spawned Easter egg
        container.set(NamespacedKeyConstants.NATURAL_EGG_KEY, PersistentDataType.BOOLEAN, true);

        if (fake) {
            // Mark egg as fake
            container.set(NamespacedKeyConstants.FAKE_EGG_KEY, PersistentDataType.BOOLEAN, true);
        }

        if (breakable) {
            // Mark egg as breakable Easter egg
            container.set(NamespacedKeyConstants.BREAKABLE_EGG_KEY, PersistentDataType.BOOLEAN, true);
        }

        return entity;
    }

    /**
     * Task to spawn random eggs
     */
    private void handleUpdate() {
        // Skip round if spawning is disabled
        if (!active)
            return;

        // Every player has the chance
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final UUID uuid = player.getUniqueId();

            // If player is not registered -> add cooldown
            if (!nextSpawns.containsKey(uuid)) {
                resetSpawnTimer(uuid);
                continue;
            }

            // If player has cooldown -> wait
            if (nextSpawns.get(uuid) > System.currentTimeMillis())
                continue;

            // Skip blacklisted players
            if (blacklist.contains(uuid)) {
                resetSpawnTimer(uuid);
                continue;
            }

            // Spawn egg around player and reset cooldown
            spawn(player.getLocation());
            resetSpawnTimer(uuid);
        }
    }

    private void handleRainingItems() {
        if (rainItems.isEmpty())
            return;

        final List<Entity> toRemove = new ArrayList<>();

        for (final Entity entity : rainItems) {
            final Location location = entity.getLocation();
            final World world = location.getWorld();

            if (!entity.isDead() && !entity.isOnGround() && !entity.isInLava() && !entity.isInWater()) {
                world.spawnParticle(Particle.DUST, location, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(java.awt.Color.HSBtoRGB(random.nextFloat(), 1, 1) & 0xFFFFFF), 1));
                continue;
            }

            toRemove.add(entity);

            if (entity.getPersistentDataContainer().has(NamespacedKeyConstants.BREAKABLE_EGG_KEY))
                entity.remove();

            world.playSound(resourceManager.getBreakSound(), location.getX(), location.getY(), location.getZ());
            world.spawnParticle(Particle.DUST, location, 50, 0.5, 0.5, 0.5, 0.1,
                    new Particle.DustOptions(Color.fromRGB(java.awt.Color.HSBtoRGB(random.nextFloat(), 1, 1) & 0xFFFFFF), 1));

            for (final Player player : world.getNearbyPlayers(location, 1)) {
                player.damage(1);
            }
        }

        rainItems.removeAll(toRemove);
    }

    /**
     * Find valid spawn location for an egg around position.
     *
     * @param center Center of the search
     * @return Valid spawning location for the egg or {@code null} if none was found
     */
    private @Nullable Location findSpawnLocation(@NotNull Location center) {
        final World world = center.getWorld();
        final List<Location> locations = new ArrayList<>();

        final int cx = center.getBlockX();
        final int cy = center.getBlockY();
        final int cz = center.getBlockZ();
        final int maxRadius = resourceManager.getMaximumRange();

        // Search every clock around center
        for (int dx = -maxRadius; dx <= maxRadius; dx++) {
            for (int dy = -maxRadius; dy <= maxRadius; dy++) {
                for (int dz = -maxRadius; dz <= maxRadius; dz++) {
                    final int x = cx + dx;
                    final int y = cy + dy;
                    final int z = cz + dz;

                    // Check if block is in range
                    if (!validDistance(cx, cy, cz, x, y, z))
                        continue;

                    // Check if block is valid
                    final Block block = world.getBlockAt(x, y, z);
                    if (!resourceManager.checkIfValidBlock(block.getType()))
                        continue;

                    // Add block to valid locations
                    locations.add(block.getLocation().add(0.5, 0.05, 0.5));
                }
            }
        }

        // Check if no location was found -> early exit
        if (locations.isEmpty())
            return null;

        // Choose random location from options
        return locations.get(random.nextInt(locations.size()));
    }

    /**
     * Check if locations are close enough together.
     *
     * @param x1 X-Coordinate of first position
     * @param y1 Y-Coordinate of first position
     * @param z1 Z-Coordinate of first position
     * @param x2 X-Coordinate of second position
     * @param y2 Y-Coordinate of second position
     * @param z2 Z-Coordinate of second position
     * @return {@code true} if positions are close enough.
     */
    private boolean validDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        final long dx = x2 - x1;
        final long dy = y2 - y1;
        final long dz = z2 - z1;

        final long distanceSquare = dx * dx + dy * dy + dz * dz;
        final int minDistanceSquare = resourceManager.getMinimumRange() * resourceManager.getMinimumRange();
        final int maxDistanceSquare = resourceManager.getMaximumRange() * resourceManager.getMaximumRange();

        return distanceSquare >= minDistanceSquare && distanceSquare <= maxDistanceSquare;
    }
}
