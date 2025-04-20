package dev.lyphium.egghunt.manager;

import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.data.EntityMode;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
public final class ResourceManager {

    /**
     * Saving delay.
     */
    public static final int SAVE_DELAY = 20 * 2;

    @Getter(AccessLevel.NONE)
    private final JavaPlugin plugin;
    @Getter(AccessLevel.NONE)
    private final Random random = new Random(System.currentTimeMillis());

    private final Set<Material> validBlocks = new HashSet<>();
    private final List<ItemStack> eggs = new ArrayList<>();

    private int totalWeight;
    private final List<EasterEggDrop> drops = new ArrayList<>();

    private Sound spawnSound, openSound, leaderboardSound, breakSound;

    private int minimumRange, maximumRange;
    private int minimumDuration, maximumDuration;
    private int lifetime;
    private int minimumLocations;

    private EntityMode entityMode;

    private int leaderboardSize, milestone;

    private int rainRadius, rainOffset, rainAmount;
    private double rainBreakProbability;

    private BukkitTask saveTask;

    private String languageOverride;
    private MiniMessageTranslationStore translationStore;

    public ResourceManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get random Easter egg item from pool.
     *
     * @return Random Easter egg item.
     */
    public @NotNull ItemStack getRandomEgg() {
        if (eggs.isEmpty()) {
            // Backup if no eggs are registered
            return new ItemStack(Material.EGG);
        } else {
            return eggs.get(random.nextInt(eggs.size())).clone();
        }
    }

    /**
     * Add a new Easter egg item to the pool.
     *
     * @param item Easter egg item to add
     */
    public void addEgg(@NotNull ItemStack item) {
        eggs.add(item);
        saveResources();
    }

    /**
     * Remove an Easter egg item from the pool.
     *
     * @param item Easter egg item to add
     */
    public void removeEgg(@NotNull ItemStack item) {
        eggs.remove(item);
        saveResources();
    }

    /**
     * Check if the provided material (block) is a valid spawn location.
     *
     * @param material Material (block) to check
     * @return {@code true} if material is valid.
     */
    public boolean checkIfValidBlock(@NotNull Material material) {
        return validBlocks.contains(material);
    }

    /**
     * Get random drop.
     *
     * @return Random drop from pool.
     */
    public @NotNull EasterEggDrop getRandomDrop() {
        // Check if at least one item is registered
        if (totalWeight == 0)
            // Backup if no drops are registered
            return new EasterEggDrop(new ItemStack(Material.COOKIE), 1, 1, 1);

        int index = random.nextInt(totalWeight);

        // Find drop where the index is smaller the weight
        for (final EasterEggDrop drop : drops) {
            if (index < drop.getWeight()) {
                return drop;
            }

            index -= drop.getWeight();
        }

        // Should never happen
        throw new IllegalStateException("No drop found");
    }

    /**
     * Add a new drop to the pool.
     *
     * @param drop Drop to be added
     */
    public void addDrop(@NotNull EasterEggDrop drop) {
        drops.add(drop);

        // Sort drops by weight
        drops.sort(Comparator.comparingInt(EasterEggDrop::getWeight).reversed());

        // Calculate sum of all weight of all drops for drop change
        totalWeight = drops.stream().mapToInt(EasterEggDrop::getWeight).sum();

        saveResources();
    }

    /**
     * Remove drop by id from pool
     *
     * @param uuid Id of the drop
     */
    public void removeDrop(@NotNull UUID uuid) {
        // Remove matching drop
        drops.removeIf(drop -> drop.getUuid().equals(uuid));

        // Calculate sum of all weight of all drops for drop change
        totalWeight = drops.stream().mapToInt(EasterEggDrop::getWeight).sum();

        saveResources();
    }

    /**
     * Load all resources, including main config, eggs and drops from files.
     */
    public void loadResources() {
        // Save default config
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        // Load configs
        final FileConfiguration config = plugin.getConfig();
        final FileConfiguration eggsConfig = loadConfig("eggs.yml");
        final FileConfiguration dropsConfig = loadConfig("drops.yml");

        // Load language override settings
        languageOverride = config.getString("LanguageOverride", "none");

        // Load entity mode
        entityMode = EntityMode.fromName(config.getString("Mode", "Item"));

        // Load range values
        minimumRange = config.getInt("Spawn.Range.Minimum", 10);
        maximumRange = config.getInt("Spawn.Range.Maximum", 50);

        // Load range duration
        minimumDuration = toSeconds(config.getString("Spawn.Duration.Minimum", "00:15:00"));
        maximumDuration = toSeconds(config.getString("Spawn.Duration.Maximum", "00:30:00"));

        // Load lifetime
        lifetime = toSeconds(config.getString("Spawn.Lifetime", "00:05:00"));

        // Load minimum locations
        minimumLocations = config.getInt("Spawn.Locations", 5);

        // Load statistic settings
        leaderboardSize = config.getInt("Statistic.Leaderboard", 5);
        milestone = config.getInt("Statistic.Milestone", 100);

        // Load rain settings
        rainRadius = config.getInt("Rain.Radius", 8);
        rainOffset = config.getInt("Rain.Offset", 30);
        rainAmount = config.getInt("Rain.Amount", 100);
        rainBreakProbability = config.getDouble("Rain.Breaking", 0.95);

        // Load sounds
        spawnSound = Sound.sound(
                Objects.requireNonNull(NamespacedKey.fromString(config.getString("Sound.Spawn.Id", "minecraft:block.sniffer_egg.plop"))),
                Sound.Source.NAMES.valueOr(config.getString("Sound.Spawn.Source", "neutral"), Sound.Source.NEUTRAL),
                (float) config.getDouble("Sound.Spawn.Volume", 1.0),
                (float) config.getDouble("Sound.Spawn.Pitch", 1.0)
        );

        openSound = Sound.sound(
                Objects.requireNonNull(NamespacedKey.fromString(config.getString("Sound.Open.Id", "minecraft:bock.sniffer_egg.hatch"))),
                Sound.Source.NAMES.valueOr(config.getString("Sound.Open.Source", "neutral"), Sound.Source.NEUTRAL),
                (float) config.getDouble("Sound.Open.Volume", 1.0),
                (float) config.getDouble("Sound.Open.Pitch", 1.0)
        );

        leaderboardSound = Sound.sound(
                Objects.requireNonNull(NamespacedKey.fromString(config.getString("Sound.Leaderboard.Id", "minecraft:entity.firework_rocket.blast"))),
                Sound.Source.NAMES.valueOr(config.getString("Sound.Leaderboard.Source", "neutral"), Sound.Source.NEUTRAL),
                (float) config.getDouble("Sound.Leaderboard.Volume", 1.0),
                (float) config.getDouble("Sound.Leaderboard.Pitch", 1.0)
        );

        breakSound = Sound.sound(
                Objects.requireNonNull(NamespacedKey.fromString(config.getString("Sound.Break.Id", "minecraft:entity.turtle.egg_break"))),
                Sound.Source.NAMES.valueOr(config.getString("Sound.Break.Source", "neutral"), Sound.Source.NEUTRAL),
                (float) config.getDouble("Sound.Break.Volume", 1.0),
                (float) config.getDouble("Sound.Break.Pitch", 1.0)
        );

        // Load valid blocks for eggs
        validBlocks.clear();
        for (final String materials : Objects.requireNonNull(config.getStringList("Locations"))) {
            final Material material = Material.matchMaterial(materials);

            if (material != null)
                validBlocks.add(material);
        }

        // Load Items for eggs
        eggs.clear();
        for (final Object o : eggsConfig.getList("Eggs", List.of())) {
            if (o instanceof ItemStack item) {
                eggs.add(item.asOne());
            }
        }

        // Load Item drops from eggs
        drops.clear();
        for (final Object o : dropsConfig.getList("Items", List.of())) {
            if (!(o instanceof Map<?, ?> map))
                continue;

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
            if (!(o instanceof Map<?, ?> map))
                continue;

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

        registerLanguages();
    }

    /**
     * Save resources to file. The action is done in the background, after small waiting time to enable additional edits.
     */
    public void saveResources() {
        if (saveTask != null)
            saveTask.cancel();

        // Delay saving, if multiple edits are made
        // Run saving asynchronously
        saveTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::saveResourceHandle, SAVE_DELAY);
    }

    /**
     * Register languages from language files.
     */
    private void registerLanguages() {
        if (translationStore != null)
            GlobalTranslator.translator().removeSource(translationStore);

        translationStore = MiniMessageTranslationStore.create(Key.key("egghunt"));
        translationStore.defaultLocale(languageOverride.equals("german") ? Locale.GERMAN : Locale.ENGLISH);

        if (languageOverride.equals("german")) {
            final ResourceBundle bundle = ResourceBundle.getBundle("egghunt", Locale.GERMAN, UTF8ResourceBundleControl.get());
            translationStore.registerAll(Locale.GERMAN, bundle, true);
        } else if (languageOverride.equals("english")) {
            final ResourceBundle bundle = ResourceBundle.getBundle("egghunt", Locale.ENGLISH, UTF8ResourceBundleControl.get());
            translationStore.registerAll(Locale.ENGLISH, bundle, true);
        } else {
            ResourceBundle bundle = ResourceBundle.getBundle("egghunt", Locale.ENGLISH, UTF8ResourceBundleControl.get());
            translationStore.registerAll(Locale.ENGLISH, bundle, true);
            bundle = ResourceBundle.getBundle("egghunt", Locale.GERMAN, UTF8ResourceBundleControl.get());
            translationStore.registerAll(Locale.GERMAN, bundle, true);
        }

        GlobalTranslator.translator().addSource(translationStore);
    }

    /**
     * Save eggs and drops to file.
     */
    private void saveResourceHandle() {
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

    /**
     * Convert time string "hh:mm:ss" to seconds.
     *
     * @param time String from which the time should be extracted
     * @return Duration in seconds.
     */
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

    /**
     * Load config from file.
     *
     * @param path Path of the config
     * @return Loaded config of the file.
     */
    private @NotNull FileConfiguration loadConfig(@NotNull String path) {
        // Save default config
        final File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }

        return YamlConfiguration.loadConfiguration(file);
    }
}
