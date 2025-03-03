package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.manager.StatisticManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EggHuntLeaderboardCommand implements SubCommand {

    private final StatisticManager statisticManager;

    public EggHuntLeaderboardCommand(@NotNull StatisticManager statisticManager) {
        this.statisticManager = statisticManager;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return false;
    }

    @Override
    public @Nullable List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
