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
import java.io.IOException;
import java.util.*;

@Getter
public final class ResourceManager {

    private final JavaPlugin plugin;
    private final Random random = new Random(System.currentTimeMillis());

    private final Set<Material> validBlocks = new HashSet<>();

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

    public @NotNull EasterEggDrop getRandomDrop() {
        int index = random.nextInt(totalWeight);

        for (final EasterEggDrop drop : drops) {
            if (index < drop.getWeight()) {
                return drop;
            }

            index -= drop.getWeight();
        }

        // Should never happen
        throw new IllegalStateException("No drop found");
    }

    public void loadResources() {
        // Save default config
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        // Load configs
        final FileConfiguration config = plugin.getConfig();
        final FileConfiguration eggsConfig = loadConfig("eggs.yml");
        final FileConfiguration dropsConfig = loadConfig("drops.yml");

        // Load range values
        minimumRange = config.getInt("Spawn.Range.Minimum", 10);
        maximumRange = config.getInt("Spawn.Range.Maximum", 50);

        // Load range duration
        minimumDuration = toSeconds(config.getString("Spawn.Duration.Minimum", "00:15:00"));
        maximumDuration = toSeconds(config.getString("Spawn.Duration.Maximum", "00:30:00"));

        // Load lifetime
        lifetime = toSeconds(config.getString("Spawn.Lifetime", "00:05:00"));

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

        validBlocks.clear();

        // Load valid blocks for eggs
        for (final String materials : Objects.requireNonNull(config.getStringList("Locations"))) {
            final Material material = Material.matchMaterial(materials);

            if (material != null)
                validBlocks.add(material);
        }

        eggs.clear();

        // Load Items for eggs
        for (final Object o : eggsConfig.getList("Eggs", List.of())) {
            if (o instanceof ItemStack item) {
                eggs.add(item.asOne());
            }
        }

        drops.clear();

        // Load Item drops from eggs
        for (final Object o : dropsConfig.getList("Items", List.of())) {
            if (!(o instanceof Map<?, ?> map)) {
                continue;
            }

            final Object itemData = map.getOrDefault("Item", null);
            final Object minimumData = map.containsKey("Minimum") ? map.get("Minimum") : 1;
            final Object maximumData = map.containsKey("Maximum") ? map.get("Maximum") : 1;
            final Object weightData = map.containsKey("Weight") ? map.get("Weight") : 1;

            if (itemData instanceof ItemStack item && minimumData instanceof Integer minimum
                    && maximumData instanceof Integer maximum && weightData instanceof Integer weight) {
                drops.add(new EasterEggDrop(item.asOne(), minimum, maximum, weight));
            }
        }

        // Load Command drops from eggs
        for (final Object o : dropsConfig.getList("Commands", List.of())) {
            if (!(o instanceof Map<?, ?> map)) {
                continue;
            }

            final Object commandData = map.getOrDefault("Command", null);
            final Object weightData = map.containsKey("Weight") ? map.get("Weight") : 1;

            if (commandData instanceof String command && weightData instanceof Integer weight) {
                drops.add(new EasterEggDrop(command, weight));
            }
        }

        // Sort drops by weight
        drops.sort(Comparator.comparingInt(EasterEggDrop::getWeight).reversed());

        // Calculate sum of all weight of all drops for drop change
        totalWeight = drops.stream().mapToInt(EasterEggDrop::getWeight).sum();
    }

    public void saveResources() {
        final FileConfiguration eggsConfig = new YamlConfiguration();
        final FileConfiguration dropsConfig = new YamlConfiguration();

        eggsConfig.set("Eggs", eggs);

        final List<Map<String, Object>> itemDrops = new ArrayList<>();
        final List<Map<String, Object>> commandDrops = new ArrayList<>();

        for (final EasterEggDrop drop : drops) {
            if (drop.getItemDrop() != null) {
                final HashMap<String, Object> map = new HashMap<>();
                map.put("Item", drop.getItemDrop());
                map.put("Minimum", drop.getMinimumAmount());
                map.put("Maximum", drop.getMaximumAmount());
                map.put("Weight", drop.getWeight());

                itemDrops.add(map);
            } else if (drop.getCommandDrop() != null) {
                final HashMap<String, Object> map = new HashMap<>();
                map.put("Command", drop.getCommandDrop());
                map.put("Weight", drop.getWeight());

                commandDrops.add(map);
            }
        }

        dropsConfig.set("Items", itemDrops);
        dropsConfig.set("Commands", commandDrops);

        // Save configs
        try {
            final File eggsFile = new File(plugin.getDataFolder(), "eggs.yml");
            final File dropsFile = new File(plugin.getDataFolder(), "drops.yml");

            eggsConfig.save(eggsFile);
            dropsConfig.save(dropsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save configs");
        }
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

    private @NotNull FileConfiguration loadConfig(@NotNull String path) {
        // Save default config
        final File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }

        return YamlConfiguration.loadConfiguration(file);
    }
}
