package dev.lyphium.egghunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.lyphium.egghunt.inventory.EasterEggInventory;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnstableApiUsage", "SameReturnValue"})
public final class EggHuntModelsCommand implements SubCommand {

    private final ResourceManager resourceManager;

    @Getter
    private final String minimumPermission = PermissionConstants.CONFIGURE;

    @Getter
    private final String name = "models";

    public EggHuntModelsCommand(@NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public LiteralCommandNode<CommandSourceStack> construct() {
        return Commands.literal(name)
                .requires(s -> s.getSender().hasPermission(minimumPermission) && s.getExecutor() instanceof Player)
                .executes(this::handle)
                .build();
    }

    private int handle(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.error.only_player")));
            return Command.SINGLE_SUCCESS;
        }

        player.openInventory(new EasterEggInventory(resourceManager, player.locale()).getInventory());
        return Command.SINGLE_SUCCESS;
    }
}
