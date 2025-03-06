package dev.lyphium.egghunt.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class EggManager {

    private final ResourceManager resourceManager;

    private final Map<UUID, Long> nexSpawns = new HashMap<>();

    private final Random random = new Random(System.currentTimeMillis());

    @Getter
    @Setter
    private boolean active = true;

    public EggManager(@NotNull JavaPlugin plugin, @NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;

        Bukkit.getScheduler().runTaskTimer(plugin, this::handleUpdate, 20, 20);
    }

    public void spawn(@NotNull Location location) {

    }

    public void resetSpawnTimer(@NotNull UUID uuid) {
        final int minimumDuration = resourceManager.getMinimumDuration();
        final int maximumDuration = resourceManager.getMaximumDuration();

        long duration = random.nextLong(minimumDuration, maximumDuration + 1);
        nexSpawns.put(uuid, duration);
    }

    public void handleUpdate() {

    }
}
