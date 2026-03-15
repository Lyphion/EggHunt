package dev.lyphium.egghunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.util.PermissionConstants;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("SameReturnValue")
public final class EggHuntSpawnCommand implements SubCommand {

    @Getter
    private final String name = "spawn";

    @Getter
    private final String minimumPermission = PermissionConstants.SPAWN;

    @Getter
    private final Component description = Component.translatable("egghunt.command.egghunt.spawn.description");

    private final EggManager eggManager;

    public EggHuntSpawnCommand(EggManager eggManager) {
        this.eggManager = eggManager;
    }

    public LiteralCommandNode<CommandSourceStack> construct() {
        return Commands.literal(name)
                .requires(s -> s.getSender().hasPermission(minimumPermission))
                .executes(this::handleSelf)
                .then(Commands.argument("player", ArgumentTypes.players())
                        .executes(this::handleOther)
                )
                .build();
    }

    private int handleSelf(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.spawn.only_player")));
            return Command.SINGLE_SUCCESS;
        }

        final boolean success = eggManager.spawn(player.getLocation(), true, false);

        if (success) {
            executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.spawn.success")));
        } else {
            executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.spawn.failure")));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int handleOther(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        final List<Player> targets;
        try {
            targets = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
        } catch (CommandSyntaxException e) {
            executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.spawn.unknown_user")));
            return Command.SINGLE_SUCCESS;
        }

        if (targets.isEmpty()) {
            executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.spawn.unknown_user")));
            return Command.SINGLE_SUCCESS;
        }

        boolean success = true;
        for (final Player target : targets) {
            success &= eggManager.spawn(target.getLocation(), true, false);
        }

        if (success) {
            executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.spawn.success")));
        } else {
            executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.spawn.failure")));
        }

        return Command.SINGLE_SUCCESS;
    }
}
