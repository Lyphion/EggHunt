package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.manager.StatisticManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EggHuntLeaderboardCommand implements SubCommand {

    public static final int TOP_COUNT = 10;

    private final JavaPlugin plugin;
    private final StatisticManager statisticManager;

    public EggHuntLeaderboardCommand(@NotNull JavaPlugin plugin, @NotNull StatisticManager statisticManager) {
        this.plugin = plugin;
        this.statisticManager = statisticManager;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 0) {
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final List<Tuple<String, Integer>> leaderboard = statisticManager.getLeaderboard(TOP_COUNT);

            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.leaderboard.menu", ColorConstants.DEFAULT)));

            for (int i = 0; i < leaderboard.size(); i++) {
                final Tuple<String, Integer> statistic = leaderboard.get(i);

                final TextComponent.Builder builder = Component.text()
                        .content("» ").color(ColorConstants.DEFAULT)
                        .append(Component.text("#" + (i + 1), ColorConstants.HIGHLIGHT))
                        .append(Component.text(": ", ColorConstants.DEFAULT))
                        .append(Component.text(statistic.getA(), ColorConstants.WHITE))
                        .append(Component.text(" (", ColorConstants.DEFAULT))
                        .append(Component.text(statistic.getB(), ColorConstants.ERROR))
                        .append(Component.text(")", ColorConstants.DEFAULT));

                sender.sendMessage(builder.build());
            }

            if (sender instanceof Player player && leaderboard.stream().noneMatch(s -> s.getA().equals(sender.getName()))) {
                final Tuple<Integer, Integer> statistic = statisticManager.getStatistic(player);

                if (statistic.getA() == -1)
                    return;

                final TextComponent.Builder builder = Component.text()
                        .content("» ").color(ColorConstants.DEFAULT)
                        .append(Component.text("#" + statistic.getA(), ColorConstants.HIGHLIGHT))
                        .append(Component.text(": ", ColorConstants.DEFAULT))
                        .append(Component.text(player.getName(), ColorConstants.WHITE))
                        .append(Component.text(" (", ColorConstants.DEFAULT))
                        .append(Component.text(statistic.getB(), ColorConstants.ERROR))
                        .append(Component.text(")", ColorConstants.DEFAULT));

                sender.sendMessage(builder.build());
            }
        });

        return true;
    }

    @Override
    public List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
