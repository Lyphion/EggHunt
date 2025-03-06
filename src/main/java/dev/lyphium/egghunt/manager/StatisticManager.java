package dev.lyphium.egghunt.manager;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class StatisticManager {

    private static final Comparator<Map.Entry<UUID, Integer>> COMPARATOR = Comparator
            .comparingInt((Map.Entry<UUID, Integer> e) -> -e.getValue())
            .thenComparing(Map.Entry::getKey);

    public final Map<UUID, Integer> statistics = new ConcurrentHashMap<>();

    public int addPoints(@NotNull Player player, int points) {
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

    }

    public void saveStatistics() {

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
