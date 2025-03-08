package dev.lyphium.egghunt.util;

import dev.lyphium.egghunt.manager.StatisticManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.minecraft.util.Tuple;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class EggHuntPlaceholderExpansion extends PlaceholderExpansion {

    private static final String TOTAL_EGGS = "total_eggs";
    private static final String PLAYER_EGGS = "player_eggs";

    private final JavaPlugin plugin;

    private final StatisticManager statisticManager;

    public EggHuntPlaceholderExpansion(
            @NotNull JavaPlugin plugin,
            @NotNull StatisticManager statisticManager
    ) {
        this.plugin = plugin;
        this.statisticManager = statisticManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getPluginMeta().getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return List.of(TOTAL_EGGS, PLAYER_EGGS);
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        return player != null ? getPoints(player.getUniqueId(), params) : getPoints(null, params);
    }

    @Override
    public @Nullable String onPlaceholderRequest(@Nullable Player player, @NotNull String params) {
        return player != null ? getPoints(player.getUniqueId(), params) : getPoints(null, params);
    }

    private @Nullable String getPoints(@Nullable UUID uuid, @NotNull String params) {
        if (params.equalsIgnoreCase(PLAYER_EGGS) && uuid != null) {
            final Tuple<Integer, Integer> statistic = statisticManager.getStatistic(uuid);
            return statistic.getB().toString();
        } else if (params.equalsIgnoreCase(TOTAL_EGGS)) {
            return String.valueOf(statisticManager.getTotalPoints());
        }

        return null;
    }
}
