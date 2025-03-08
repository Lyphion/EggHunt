package dev.lyphium.egghunt.manager;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class StatisticManager {

    /**
     * Comparer to sort list of ranking (highest score first)
     */
    private static final Comparator<Tuple<UUID, Integer>> COMPARATOR = Comparator
            .comparingInt((Tuple<UUID, Integer> e) -> -e.getB())
            .thenComparing(Tuple::getA);

    /**
     * Period after which the statistics are saved. (If something changed)
     */
    private static final int SAVE_PERIOD = 60 * 20;

    private final JavaPlugin plugin;

    /**
     * Lookup for the score of all players.
     */
    private final Map<UUID, Integer> statistics = new HashMap<>();

    /**
     * Sorted list, where the index is the rank.
     */
    private final List<Tuple<UUID, Integer>> ranking = new ArrayList<>();

    /**
     * Lock to ensure concurrent operation.
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Whether the statistics have changed.
     */
    private boolean changed;

    public StatisticManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveStatistics, SAVE_PERIOD, SAVE_PERIOD);
    }

    /**
     * Add points to a player.
     *
     * @param player Player for which the points should be added
     * @param points Amount of points to add
     * @return New amount of points.
     */
    public int addPoints(@NotNull Player player, int points) {
        lock.writeLock().lock();

        try {
            // Update total points
            final int total = statistics.merge(player.getUniqueId(), points, Integer::sum);
            changed = true;

            // Remove old ranking entry (if exists)
            final int oldIndex = Collections.binarySearch(ranking, new Tuple<>(player.getUniqueId(), total - points), COMPARATOR);
            if (oldIndex >= 0)
                ranking.remove(oldIndex);

            // Add new ranking entry
            final Tuple<UUID, Integer> element = new Tuple<>(player.getUniqueId(), total);
            final int newIndex = Collections.binarySearch(ranking, element, COMPARATOR);
            ranking.add(~newIndex, element);

            return total;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get statistic of player as tuple of rank and points.
     *
     * @param player Player for whom the statistic should be queried
     * @return Statistic of the player.
     */
    public @NotNull Tuple<Integer, Integer> getStatistic(@NotNull Player player) {
        lock.readLock().lock();

        try {
            int points = statistics.getOrDefault(player.getUniqueId(), 0);

            // If no points are scored -> no ranking
            if (points == 0)
                return new Tuple<>(-1, 0);

            // Get ranking of player by the index in ranking
            final int rank = Collections.binarySearch(ranking, new Tuple<>(player.getUniqueId(), points), COMPARATOR) + 1;
            return new Tuple<>(rank, points);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get leaderboard of highest points.
     *
     * @param count Amount of players to query
     * @return List of top players.
     */
    public @NotNull List<Tuple<String, Integer>> getLeaderboard(int count) {
        lock.readLock().lock();

        final List<CompletableFuture<Tuple<String, Integer>>> futures;
        try {
            futures = ranking.stream()
                    .limit(count)
                    .map(e -> getName(e.getA()).thenApply(n -> new Tuple<>(n.orElse("ERROR"), e.getB())))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * Load statistic from file.
     */
    public void loadStatistics() {
        lock.writeLock().lock();

        // Save default config
        final File file = new File(plugin.getDataFolder(), "statistics.yml");
        var config = YamlConfiguration.loadConfiguration(file);

        try {
            // Clear old statistic
            statistics.clear();
            ranking.clear();

            // Load new values
            for (final String key : config.getKeys(false)) {
                final UUID uuid = UUID.fromString(key);
                final int points = config.getInt(key);

                statistics.put(uuid, points);
                ranking.add(new Tuple<>(uuid, points));
            }

            // Recalculate ranking
            ranking.sort(COMPARATOR);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Save statistic to file.
     */
    public void saveStatistics() {
        // If nothing changed -> nothing to do
        if (!changed)
            return;

        lock.readLock().lock();
        final YamlConfiguration config = new YamlConfiguration();

        try {
            for (final Map.Entry<UUID, Integer> entry : statistics.entrySet()) {
                config.set(entry.getKey().toString(), entry.getValue());
            }
        } finally {
            lock.readLock().unlock();
        }

        // Save configs
        try {
            final File file = new File(plugin.getDataFolder(), "statistics.yml");
            config.save(file);
            changed = false;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save configs");
        }
    }

    /**
     * Get name of player by uuid.
     *
     * @param uuid UUID of player.
     * @return Optional name of the player.
     */
    private static @NotNull CompletableFuture<Optional<String>> getName(@NotNull UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);

        if (player != null)
            return CompletableFuture.completedFuture(Optional.of(player.getName()));

        try {
            return SkullBlockEntity.fetchGameProfile(uuid, null).thenApply(g -> g.map(GameProfile::getName));
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }
}
