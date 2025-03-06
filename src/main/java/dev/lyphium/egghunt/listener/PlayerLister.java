package dev.lyphium.egghunt.listener;

import dev.lyphium.egghunt.manager.EggManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerLister implements Listener {

    private final EggManager eggManager;

    public PlayerLister(EggManager eggManager) {
        this.eggManager = eggManager;
    }

    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        eggManager.resetSpawnTimer(event.getPlayer().getUniqueId());
    }

}
