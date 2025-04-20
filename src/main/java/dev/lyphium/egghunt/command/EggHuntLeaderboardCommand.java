package dev.lyphium.egghunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.manager.StatisticManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage", "SameReturnValue"})
public final class EggHuntLeaderboardCommand implements SubCommand {

    private final JavaPlugin plugin;

    private final ResourceManager resourceManager;
    private final StatisticManager statisticManager;

    @Getter
    private final String minimumPermission = PermissionConstants.LEADERBOARD;

    @Getter
    private final String name = "leaderboard";

    public EggHuntLeaderboardCommand(
            @NotNull JavaPlugin plugin,
            @NotNull ResourceManager resourceManager,
            @NotNull StatisticManager statisticManager
    ) {
        this.plugin = plugin;
        this.resourceManager = resourceManager;
        this.statisticManager = statisticManager;
    }

    public LiteralCommandNode<CommandSourceStack> construct() {
        return Commands.literal(name)
                .requires(s -> s.getSender().hasPermission(minimumPermission))
                .executes(this::handle)
                .build();
    }

    private int handle(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        // Leaderboard is calculated async to relax main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Get leaderboard
            final List<Tuple<String, Integer>> leaderboard = statisticManager.getLeaderboard(resourceManager.getLeaderboardSize());

            executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.leaderboard.menu")));

            // Send top players
            for (int i = 0; i < leaderboard.size(); i++) {
                final Tuple<String, Integer> statistic = leaderboard.get(i);

                final TextComponent.Builder builder = Component.text()
                        .content("» ").color(NamedTextColor.GRAY)
                        .append(Component.text("#" + (i + 1), ColorConstants.HIGHLIGHT))
                        .append(Component.text(": ", NamedTextColor.GRAY))
                        .append(Component.text(statistic.getA(), executor instanceof Player && statistic.getA().equals(executor.getName()) ? NamedTextColor.GREEN : NamedTextColor.WHITE))
                        .append(Component.text(" (", NamedTextColor.GRAY))
                        .append(Component.text(statistic.getB(), NamedTextColor.RED))
                        .append(Component.text(")", NamedTextColor.GRAY));

                executor.sendMessage(builder.build());
            }

            // If player is not contained, extend the leaderboard
            if (executor instanceof Player player && leaderboard.stream().noneMatch(s -> s.getA().equals(executor.getName()))) {
                final Tuple<Integer, Integer> statistic = statisticManager.getStatistic(player.getUniqueId());

                if (statistic.getA() == -1)
                    return;

                final TextComponent.Builder builder = Component.text()
                        .content("» ").color(NamedTextColor.GRAY)
                        .append(Component.text("#" + statistic.getA(), ColorConstants.HIGHLIGHT))
                        .append(Component.text(": ", NamedTextColor.GRAY))
                        .append(Component.text(player.getName(), NamedTextColor.GREEN))
                        .append(Component.text(" (", NamedTextColor.GRAY))
                        .append(Component.text(statistic.getB(), NamedTextColor.RED))
                        .append(Component.text(")", NamedTextColor.GRAY));

                executor.sendMessage(builder.build());
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
