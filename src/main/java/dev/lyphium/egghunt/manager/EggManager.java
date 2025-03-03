package dev.lyphium.egghunt.manager;

import dev.lyphium.egghunt.data.EasterEgg;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class EggManager {

    private final ResourceManager resourceManager;

    private final List<EasterEgg> activeEggs = new ArrayList<>();

    public EggManager(@NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void spawn(@NotNull Player player) {

    }

    public void vanish(@NotNull Player player) {

    }

    public void pickup(@NotNull Player player) {

    }

    public void update() {

    }
}
