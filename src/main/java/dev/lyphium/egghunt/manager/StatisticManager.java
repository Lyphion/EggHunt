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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class StatisticManager {

    private static final Comparator<Map.Entry<UUID, Integer>> COMPARATOR = Comparator
            .comparingInt((Map.Entry<UUID, Integer> e) -> -e.getValue())
            .thenComparing(Map.Entry::getKey);

    public static final int SAVE_PERIOD = 60 * 20;

    private final JavaPlugin plugin;

    private final Map<UUID, Integer> statistics = new ConcurrentHashMap<>();
    private boolean changed;

    public StatisticManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveStatistics, SAVE_PERIOD, SAVE_PERIOD);
    }

    public int addPoints(@NotNull Player player, int points) {
        changed = true;
        return statistics.merge(player.getUniqueId(), points, Integer::sum);
    }

    public Tuple<Integer, Integer> getStatistic(@NotNull Player player) {
        int points = statistics.getOrDefault(player.getUniqueId(), 0);

        if (points == 0) {
            return new Tuple<>(-1, 0);
        }

        long rank = statistics.entrySet().stream()
                .sorted(COMPARATOR)
                .takeWhile(e -> !e.getKey().equals(player.getUniqueId()))
                .count() + 1;

        return new Tuple<>((int) rank, points);
    }

    public @NotNull List<Tuple<String, Integer>> getLeaderboard(int count) {
        return statistics.entrySet().stream()
                .sorted(COMPARATOR)
                .limit(count)
                .map(e -> getName(e.getKey()).thenApply(n -> new Tuple<>(n.orElse("ERROR"), e.getValue())))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public void loadStatistics() {
        // Save default config
        final File file = new File(plugin.getDataFolder(), "statistics.yml");
        var config = YamlConfiguration.loadConfiguration(file);

        statistics.clear();
        for (final String key : config.getKeys(false)) {
            final UUID uuid = UUID.fromString(key);
            final int points = config.getInt(key);

            statistics.put(uuid, points);
        }
    }

    public void saveStatistics() {
        if (!changed)
            return;

        final YamlConfiguration config = new YamlConfiguration();

        for (final Map.Entry<UUID, Integer> entry : statistics.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
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

    private static @NotNull CompletableFuture<Optional<String>> getName(@NotNull UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            return CompletableFuture.completedFuture(Optional.of(player.getName()));
        }

        try {
            return SkullBlockEntity.fetchGameProfile(uuid, null).thenApply(g -> g.map(GameProfile::getName));
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }
}
