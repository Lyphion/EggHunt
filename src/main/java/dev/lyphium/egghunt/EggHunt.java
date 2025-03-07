package dev.lyphium.egghunt;

import dev.lyphium.egghunt.command.EggHuntCommand;
import dev.lyphium.egghunt.inventory.DropsInventory;
import dev.lyphium.egghunt.inventory.EasterEggInventory;
import dev.lyphium.egghunt.listener.InventoryListener;
import dev.lyphium.egghunt.listener.PlayerListener;
import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.manager.StatisticManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public final class EggHunt extends JavaPlugin {

    private static EggHunt instance;

    private ResourceManager resourceManager;
    private EggManager eggManager;
    private StatisticManager statisticManager;

    public EggHunt() {
        instance = this;
    }

    @Override
    public void onEnable() {
        resourceManager = new ResourceManager(this);
        eggManager = new EggManager(this, resourceManager);
        statisticManager = new StatisticManager(this);

        resourceManager.loadResources();
        statisticManager.loadStatistics();

        registerLanguages();
        registerCommands();
        registerListeners();

        getLogger().info("Plugin activated");
    }

    @Override
    public void onDisable() {
        statisticManager.saveStatistics();

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getOpenInventory().getTopInventory().getHolder() instanceof EasterEggInventory || p.getOpenInventory().getTopInventory().getHolder() instanceof DropsInventory)
                .forEach(HumanEntity::closeInventory);

        getLogger().info("Plugin deactivated");
    }

    /**
     * Register languages from language files.
     */
    private void registerLanguages() {
        final TranslationRegistry registry = TranslationRegistry.create(Key.key("egghunt"));
        registry.defaultLocale(Locale.ENGLISH);

        ResourceBundle bundle = ResourceBundle.getBundle("egghunt", Locale.ENGLISH, UTF8ResourceBundleControl.get());
        registry.registerAll(Locale.ENGLISH, bundle, true);
        bundle = ResourceBundle.getBundle("egghunt", Locale.GERMAN, UTF8ResourceBundleControl.get());
        registry.registerAll(Locale.GERMAN, bundle, true);

        GlobalTranslator.translator().addSource(registry);
    }

    private void registerCommands() {
        new EggHuntCommand(this, resourceManager, eggManager, statisticManager)
                .register(Objects.requireNonNull(getCommand("egghunt")));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(resourceManager, statisticManager), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
    }

    public static @NotNull Logger getPluginLogger() {
        return instance.getLogger();
    }
}
