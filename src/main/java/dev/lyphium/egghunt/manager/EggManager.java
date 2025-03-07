package dev.lyphium.egghunt.manager;

import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
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

    public static final Component USAGE_DESCRIPTION = Component.translatable("tutorial.socialInteractions.description", ColorConstants.DEFAULT,
                    Component.keybind("key.sneak").append(Component.text("+")).append(Component.keybind("key.mouse.left")))
            .decoration(TextDecoration.ITALIC, false);

    private final ResourceManager resourceManager;

    private final Map<UUID, Long> nextSpawns = new HashMap<>();

    private final Random random = new Random(System.currentTimeMillis());

    @Getter
    private boolean active;

    public EggManager(@NotNull JavaPlugin plugin, @NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;

        Bukkit.getScheduler().runTaskTimer(plugin, this::handleUpdate, 20, 20);
        setActive(true);
    }

    public void setActive(boolean active) {
        nextSpawns.clear();
        this.active = active;
    }

    public void spawn(@NotNull Location location) {
        final Location spawn = findSpawnLocation(location);

        if (spawn == null)
            return;

        final ItemStack item;
        if (resourceManager.getEggs().isEmpty()) {
            // Backup if no eggs are registered
            item = new ItemStack(Material.EGG);
        } else {
            item = resourceManager.getEggs().get(random.nextInt(resourceManager.getEggs().size())).clone();
        }

        item.editMeta(meta -> {
            final PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(NamespacedKeyConstants.NATURAL_EGG_KEY, PersistentDataType.BOOLEAN, true);
            container.set(NamespacedKeyConstants.EASTER_EGG_KEY, PersistentDataType.BOOLEAN, true);

            final List<Component> lore = meta.hasLore() ? Objects.requireNonNull(meta.lore()) : new ArrayList<>();

            // Failsafe to only include description once
            if (lore.contains(USAGE_DESCRIPTION))
                return;

            if (!lore.isEmpty())
                lore.add(Component.empty());
            lore.add(USAGE_DESCRIPTION);

            meta.lore(lore);
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
            final String format = Objects.requireNonNull(GlobalTranslator.translator().translate("spawn.egg", player.locale())).format(null);
            player.sendActionBar(MiniMessage.miniMessage().deserialize(format));
        }
    }

    public void resetSpawnTimer(@NotNull UUID uuid) {
        final int minimumDuration = resourceManager.getMinimumDuration();
        final int maximumDuration = resourceManager.getMaximumDuration();

        long duration = random.nextLong(minimumDuration, maximumDuration + 1);
        nextSpawns.put(uuid, System.currentTimeMillis() + duration * 1000);
    }

    public void handleUpdate() {
        if (!active)
            return;

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final UUID uuid = player.getUniqueId();

            if (!nextSpawns.containsKey(uuid)) {
                resetSpawnTimer(uuid);
                continue;
            }

            if (nextSpawns.get(uuid) > System.currentTimeMillis())
                continue;

            spawn(player.getLocation());
            resetSpawnTimer(uuid);
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
