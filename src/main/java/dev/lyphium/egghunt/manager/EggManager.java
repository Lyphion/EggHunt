package dev.lyphium.egghunt.manager;

import dev.lyphium.egghunt.data.EasterEgg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class EggManager {

    private final ResourceManager resourceManager;

    private final List<EasterEgg> activeEggs = new ArrayList<>();

    public EggManager(@NotNull JavaPlugin plugin, @NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;

        Bukkit.getScheduler().runTaskTimer(plugin, this::handleUpdate, 20, 20);
    }

    public void spawn(@NotNull Player player) {

    }

    public void vanish(@NotNull Player player) {

    }

    public void pickup(@NotNull Player player) {

    }

    public void removeAll() {

    }

    public void handleUpdate() {

    }
}
