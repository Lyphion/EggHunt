package dev.lyphium.egghunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

@SuppressWarnings({"UnstableApiUsage", "SameReturnValue"})
public final class EggHuntReloadCommand implements SubCommand {

    private final JavaPlugin plugin;

    private final ResourceManager resourceManager;
    private final EggManager eggManager;

    @Getter
    private final String minimumPermission = PermissionConstants.CONFIGURE;

    @Getter
    private final String name = "reload";

    public EggHuntReloadCommand(
            @NotNull JavaPlugin plugin,
            @NotNull ResourceManager resourceManager,
            @NotNull EggManager eggManager
    ) {
        this.plugin = plugin;
        this.resourceManager = resourceManager;
        this.eggManager = eggManager;
    }

    public LiteralCommandNode<CommandSourceStack> construct() {
        return Commands.literal(name)
                .requires(s -> s.getSender().hasPermission(minimumPermission))
                .executes(this::handle)
                .build();
    }

    private int handle(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        try {
            eggManager.setActive(false);
            resourceManager.loadResources();
            eggManager.setActive(true);

            executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.reload.success")));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload configurations", e);
            executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.reload.failure")));
        }

        return Command.SINGLE_SUCCESS;
    }
}
