package dev.lyphium.egghunt.listener;

import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class ChunkListener implements Listener {

    @EventHandler
    private void onUnload(@NotNull ChunkUnloadEvent event) {
        Arrays.stream(event.getChunk().getEntities()).forEach(this::checkEgg);
    }

    @EventHandler
    private void onUnload(@NotNull EntitiesUnloadEvent event) {
        event.getEntities().forEach(this::checkEgg);
    }

    @EventHandler
    private void onLoad(@NotNull ChunkLoadEvent event) {
        Arrays.stream(event.getChunk().getEntities()).forEach(this::checkEgg);
    }

    @EventHandler
    private void onLoad(@NotNull EntitiesLoadEvent event) {
        event.getEntities().forEach(this::checkEgg);
    }

    private void checkEgg(@NotNull Entity entity) {
        if (entity.getPersistentDataContainer().has(NamespacedKeyConstants.NATURAL_EGG_KEY)) {
            entity.remove();
        }
    }
}
