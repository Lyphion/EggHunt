package dev.lyphium.egghunt;

import dev.lyphium.egghunt.command.EggHuntCommand;
import dev.lyphium.egghunt.inventory.DropsInventory;
import dev.lyphium.egghunt.inventory.EasterEggInventory;
import dev.lyphium.egghunt.listener.ChunkListener;
import dev.lyphium.egghunt.listener.EntityListener;
import dev.lyphium.egghunt.listener.InventoryListener;
import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.manager.StatisticManager;
import dev.lyphium.egghunt.util.EggHuntPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class EggHunt extends JavaPlugin {

    private ResourceManager resourceManager;
    private EggManager eggManager;
    private StatisticManager statisticManager;

    @Override
    public void onEnable() {
        resourceManager = new ResourceManager(this);
        statisticManager = new StatisticManager(this);
        eggManager = new EggManager(this, resourceManager, statisticManager);

        resourceManager.loadResources();
        statisticManager.loadStatistics();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EggHuntPlaceholderExpansion(this, statisticManager).register();
        }

        registerCommands();
        registerListeners();

        getLogger().info("Plugin activated");
    }

    @Override
    public void onDisable() {
        statisticManager.saveStatistics();
        eggManager.removeAllEggs();

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getOpenInventory().getTopInventory().getHolder() instanceof EasterEggInventory || p.getOpenInventory().getTopInventory().getHolder() instanceof DropsInventory)
                .forEach(HumanEntity::closeInventory);

        getLogger().info("Plugin deactivated");
    }

    private void registerCommands() {
        new EggHuntCommand(this, resourceManager, eggManager, statisticManager)
                .register(Objects.requireNonNull(getCommand("egghunt")));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new EntityListener(resourceManager, eggManager), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
    }
}
