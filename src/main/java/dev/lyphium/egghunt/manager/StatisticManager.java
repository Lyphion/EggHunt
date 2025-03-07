package dev.lyphium.egghunt.manager;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
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

    private static final Comparator<Tuple<UUID, Integer>> COMPARATOR = Comparator
            .comparingInt((Tuple<UUID, Integer> e) -> -e.getB())
            .thenComparing(Tuple::getA);

    public static final int SAVE_PERIOD = 60 * 20;

    private final JavaPlugin plugin;

    @Getter
    private final Map<UUID, Integer> statistics = new ConcurrentHashMap<>();
    private final List<Tuple<UUID, Integer>> ranking = new ArrayList<>();

    private boolean changed;

    public StatisticManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveStatistics, SAVE_PERIOD, SAVE_PERIOD);
    }

    public int addPoints(@NotNull Player player, int points) {
        final int total = statistics.merge(player.getUniqueId(), points, Integer::sum);
        changed = true;

        final int oldIndex = Collections.binarySearch(ranking, new Tuple<>(player.getUniqueId(), total - points), COMPARATOR);
        ranking.remove(oldIndex);

        final Tuple<UUID, Integer> element = new Tuple<>(player.getUniqueId(), total);
        final int newIndex = Collections.binarySearch(ranking, element, COMPARATOR);
        ranking.add(~newIndex, element);

        return total;
    }

    public @NotNull Tuple<Integer, Integer> getStatistic(@NotNull Player player) {
        int points = statistics.getOrDefault(player.getUniqueId(), 0);

        if (points == 0)
            return new Tuple<>(-1, 0);

        final int rank = Collections.binarySearch(ranking, new Tuple<>(player.getUniqueId(), points), COMPARATOR) + 1;
        return new Tuple<>(rank, points);
    }

    public @NotNull List<Tuple<String, Integer>> getLeaderboard(int count) {
        return ranking.stream()
                .limit(count)
                .map(e -> getName(e.getA()).thenApply(n -> new Tuple<>(n.orElse("ERROR"), e.getB())))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public void loadStatistics() {
        // Save default config
        final File file = new File(plugin.getDataFolder(), "statistics.yml");
        var config = YamlConfiguration.loadConfiguration(file);

        statistics.clear();
        ranking.clear();

        for (final String key : config.getKeys(false)) {
            final UUID uuid = UUID.fromString(key);
            final int points = config.getInt(key);

            statistics.put(uuid, points);
            ranking.add(new Tuple<>(uuid, points));
        }

        ranking.sort(COMPARATOR);
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

        if (player != null)
            return CompletableFuture.completedFuture(Optional.of(player.getName()));

        try {
            return SkullBlockEntity.fetchGameProfile(uuid, null).thenApply(g -> g.map(GameProfile::getName));
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }
}
