package dev.lyphium.egghunt.listener;

import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

import java.util.Arrays;

public final class ChunkListener implements Listener {

    @EventHandler
    private void onUnload(ChunkUnloadEvent event) {
        Arrays.stream(event.getChunk().getEntities()).forEach(this::checkEgg);
    }

    @EventHandler
    private void onUnload(EntitiesUnloadEvent event) {
        event.getEntities().forEach(this::checkEgg);
    }

    @EventHandler
    private void onLoad(ChunkLoadEvent event) {
        Arrays.stream(event.getChunk().getEntities()).forEach(this::checkEgg);
    }

    @EventHandler
    private void onLoad(EntitiesLoadEvent event) {
        event.getEntities().forEach(this::checkEgg);
    }

    private void checkEgg(Entity entity) {
        if (entity.getPersistentDataContainer().has(NamespacedKeyConstants.NATURAL_EGG_KEY)) {
            entity.remove();
        }
    }
}
