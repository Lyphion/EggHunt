package dev.lyphium.egghunt.manager;

import dev.lyphium.egghunt.data.EasterEggDrop;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public final class ResourceManager {

    private final JavaPlugin plugin;

    private final List<Material> validBlocks = new ArrayList<>();

    private final List<ItemStack> eggs = new ArrayList<>();

    private int totalWeight;
    private final List<EasterEggDrop> drops = new ArrayList<>();

    private Sound spawnSound, openSound;

    private int minimumRange, maximumRange;
    private int minimumDuration, maximumDuration;
    private int lifetime;

    public ResourceManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadResources() {
        // Save default config
        plugin.saveDefaultConfig();

        // Save default eggs config
        final File eggsFile = new File(plugin.getDataFolder(), "eggs.yml");
        if (!eggsFile.exists()) {
            plugin.saveResource("eggs.yml", false);
        }

        // Save default drops config
        final File dropsFile = new File(plugin.getDataFolder(), "drops.yml");
        if (!dropsFile.exists()) {
            plugin.saveResource("drops.yml", false);
        }

        final FileConfiguration config = plugin.getConfig();
        final FileConfiguration eggsConfig = YamlConfiguration.loadConfiguration(eggsFile);
        final FileConfiguration dropsConfig = YamlConfiguration.loadConfiguration(dropsFile);

        // Load range values
        minimumRange = config.getInt("Spawn.Range.Minimum", 10);
        maximumRange = config.getInt("Spawn.Range.Maximum", 50);

        // Load range duration
        minimumDuration = toSeconds(config.getString("Spawn.Duration.Minimum", "00:15:00"));
        maximumDuration = toSeconds(config.getString("Spawn.Duration.Maximum", "00:30:00"));

        // Load lifetime
        lifetime = toSeconds(config.getString("Spawn.Duration.Lifetime", "00:05:00"));

        // Load sounds
        spawnSound = Sound.sound(
                Objects.requireNonNull(NamespacedKey.fromString(config.getString("Sound.Spawn.Id", "minecraft:block.sniffer_egg.plop"))),
                Sound.Source.NAMES.valueOr(config.getString("Sound.Spawn.Source", "neutral"), Sound.Source.NEUTRAL),
                (float) config.getDouble("Sound.Spawn.Volume"),
                (float) config.getDouble("Sound.Spawn.Pitch")
        );

        openSound = Sound.sound(
                Objects.requireNonNull(NamespacedKey.fromString(config.getString("Sound.Open.Id", "minecraft:bock.sniffer_egg.hatch"))),
                Sound.Source.NAMES.valueOr(config.getString("Sound.Open.Source", "neutral"), Sound.Source.NEUTRAL),
                (float) config.getDouble("Sound.Open.Volume"),
                (float) config.getDouble("Sound.Open.Pitch")
        );

        // Load valid blocks for eggs
        for (final String materials : Objects.requireNonNull(config.getStringList("Locations"))) {
            final Material material = Material.matchMaterial(materials);

            if (material != null)
                validBlocks.add(material);
        }

        // Load Items for eggs
        for (final Object o : eggsConfig.getList("Eggs", List.of())) {
            if (o instanceof ItemStack item) {
                eggs.add(item.asOne());
            }
        }

        // Load Item drops from eggs
        for (final Object o : dropsConfig.getList("Drops.Items", List.of())) {
            if (o instanceof Map<?, ?> map) {
                final Object itemData = map.getOrDefault("Item", null);
                final Object minimumData = map.containsKey("Minimum") ? map.get("Minimum") : 1;
                final Object maximumData = map.containsKey("Maximum") ? map.get("Maximum") : 1;
                final Object weightData = map.containsKey("Weight") ? map.get("Weight") : 1;

                if (itemData instanceof ItemStack item && minimumData instanceof Integer minimum
                        && maximumData instanceof Integer maximum && weightData instanceof Integer weight) {
                    drops.add(new EasterEggDrop(item.asOne(), minimum, maximum, weight));
                }
            }
        }

        // Load Command drops from eggs
        for (final Object o : dropsConfig.getList("Drops.Commands", List.of())) {
            if (o instanceof Map<?, ?> map) {
                final Object commandData = map.getOrDefault("Command", null);
                final Object weightData = map.containsKey("Weight") ? map.get("Weight") : 1;

                if (commandData instanceof String command && weightData instanceof Integer weight) {
                    drops.add(new EasterEggDrop(command, weight));
                }
            }
        }

        // Calculate sum of all weight of all drops for drop change
        totalWeight = drops.stream().mapToInt(EasterEggDrop::getWeight).sum();
    }

    public void saveResources() {
        final FileConfiguration config = plugin.getConfig();


        plugin.saveConfig();
    }

    private int toSeconds(@NotNull String time) {
        final String[] parts = time.split(":");
        int seconds = 0;

        int i = 0;
        if (parts.length > 2)
            seconds += Integer.parseInt(parts[i++]) * 60 * 60;
        if (parts.length > 1)
            seconds += Integer.parseInt(parts[i++]) * 60;
        seconds += Integer.parseInt(parts[i]);

        return seconds;
    }
}
