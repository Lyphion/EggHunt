package dev.lyphium.egghunt.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.manager.StatisticManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main command for managing the plugin.
 */
@SuppressWarnings("UnstableApiUsage")
public final class EggHuntCommand {

    public static final String DESCRIPTION = "Central command for egg hunt";

    /**
     * Collection of all available sub commands.
     */
    @Getter(AccessLevel.PACKAGE)
    private final SubCommand[] subCommands;

    public EggHuntCommand(
            @NotNull JavaPlugin plugin,
            @NotNull ResourceManager resourceManager,
            @NotNull EggManager eggManager,
            @NotNull StatisticManager statisticManager
    ) {
        subCommands = new SubCommand[]{
                new EggHuntDropsCommand(resourceManager),
                new EggHuntFakeCommand(eggManager),
                new EggHuntFindCommand(resourceManager),
                new EggHuntHelpCommand(this),
                new EggHuntLeaderboardCommand(plugin, resourceManager, statisticManager),
                new EggHuntModelsCommand(resourceManager),
                new EggHuntRainCommand(eggManager),
                new EggHuntReloadCommand(plugin, resourceManager, eggManager),
                new EggHuntSpawnCommand(eggManager),
                new EggHuntToggleCommand(eggManager)
        };
    }

    public LiteralCommandNode<CommandSourceStack> construct() {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal("egghunt");

        for (final SubCommand command : subCommands) {
            cmd = cmd.then(command.construct());
        }

        return cmd.build();
    }

}
