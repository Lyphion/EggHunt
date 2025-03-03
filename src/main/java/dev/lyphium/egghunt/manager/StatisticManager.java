package dev.lyphium.egghunt.manager;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class StatisticManager {

    public final Map<UUID, Integer> statistics = new HashMap<>();

    public void addPoints(@NotNull Player player, int points) {
        statistics.merge(player.getUniqueId(), points, Integer::sum);
    }

    public int getPoints(@NotNull Player player) {
        return statistics.getOrDefault(player.getUniqueId(), 0);
    }

    public @NotNull List<Tuple<String, Integer>> getLeaderboard(int count) {
        return statistics.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
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
