package dev.lyphium.egghunt.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EggManager {

    private final ResourceManager resourceManager;

    private boolean active = true;

    public EggManager(@NotNull JavaPlugin plugin, @NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;

        Bukkit.getScheduler().runTaskTimer(plugin, this::handleUpdate, 20, 20);
    }

    public void spawn(@NotNull Location location) {

    }

    public void handleUpdate() {

    }
}
