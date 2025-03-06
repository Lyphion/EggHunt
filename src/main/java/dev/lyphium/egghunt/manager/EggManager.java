package dev.lyphium.egghunt.manager;

import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class EggManager {

    private final ResourceManager resourceManager;

    private final Map<UUID, Long> nextSpawns = new HashMap<>();

    private final Random random = new Random(System.currentTimeMillis());

    @Getter
    @Setter
    private boolean active = true;

    public EggManager(@NotNull JavaPlugin plugin, @NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;

        Bukkit.getScheduler().runTaskTimer(plugin, this::handleUpdate, 20, 20);
        Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).forEach(this::resetSpawnTimer);
    }

    public void spawn(@NotNull Location location) {
        final Location spawn = findSpawnLocation(location);

        if (spawn == null)
            return;

        final ItemStack item;
        if (resourceManager.getEggs().isEmpty()) {
            item = new ItemStack(Material.EGG);
        } else {
            item = resourceManager.getEggs().get(random.nextInt(resourceManager.getEggs().size()));
        }

        item.editMeta(i -> {
            final PersistentDataContainer container = i.getPersistentDataContainer();
            container.set(NamespacedKeyConstants.NATURAL_EGG_KEY, PersistentDataType.BOOLEAN, true);
            container.set(NamespacedKeyConstants.EASTER_EGG_KEY, PersistentDataType.BOOLEAN, true);
        });

        final World world = spawn.getWorld();
        world.spawn(spawn, Item.class, i -> {
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

        for (final Player player : world.getNearbyPlayers(spawn, resourceManager.getMaximumRange())) {
            player.playSound(resourceManager.getSpawnSound());
            player.sendActionBar(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(GlobalTranslator.translator().translate("spawn.egg", player.locale())).format(null)));
        }
    }

    public void resetSpawnTimer(@NotNull UUID uuid) {
        final int minimumDuration = resourceManager.getMinimumDuration();
        final int maximumDuration = resourceManager.getMaximumDuration();

        long duration = random.nextLong(minimumDuration, maximumDuration + 1);
        nextSpawns.put(uuid, System.currentTimeMillis() + duration * 1000);
    }

    public void handleUpdate() {
        final List<UUID> removals = new ArrayList<>();

        for (final Map.Entry<UUID, Long> entry : nextSpawns.entrySet()) {
            final Player player = Bukkit.getPlayer(entry.getKey());

            if (player == null || !player.isOnline()) {
                removals.add(entry.getKey());
                continue;
            }

            if (entry.getValue() > System.currentTimeMillis())
                continue;

            spawn(player.getLocation());
            resetSpawnTimer(entry.getKey());
        }

        // Remove outdated players
        for (final UUID uuid : removals) {
            nextSpawns.remove(uuid);
        }
    }

    private @Nullable Location findSpawnLocation(@NotNull Location center) {
        final World world = center.getWorld();
        final List<Location> locations = new ArrayList<>();

        final int cx = center.getBlockX();
        final int cy = center.getBlockY();
        final int cz = center.getBlockZ();
        final int maxRadius = resourceManager.getMaximumRange();

        for (int dx = -maxRadius; dx <= maxRadius; dx++) {
            for (int dy = -maxRadius; dy <= maxRadius; dy++) {
                for (int dz = -maxRadius; dz <= maxRadius; dz++) {
                    final int x = cx + dx;
                    final int y = cy + dy;
                    final int z = cz + dz;

                    if (!validDistance(cx, cy, cz, x, y, z))
                        continue;

                    final Block block = world.getBlockAt(x, y, z);
                    if (resourceManager.getValidBlocks().contains(block.getType())) {
                        locations.add(block.getLocation().add(0.5, 0.05, 0.5));
                    }
                }
            }
        }

        return locations.get(random.nextInt(locations.size()));
    }

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
