package dev.lyphium.egghunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnstableApiUsage", "SameReturnValue"})
public final class EggHuntToggleCommand implements SubCommand {

    private final EggManager eggManager;

    @Getter
    private final String minimumPermission = PermissionConstants.TOGGLE;

    @Getter
    private final String name = "toggle";

    public EggHuntToggleCommand(@NotNull EggManager eggManager) {
        this.eggManager = eggManager;
    }

    public LiteralCommandNode<CommandSourceStack> construct() {
        return Commands.literal(name)
                .requires(s -> s.getSender().hasPermission(minimumPermission))
                .executes(this::handleGlobal)
                .then(Commands.argument("player", ArgumentTypes.players())
                        .executes(this::handlePlayer))
                .build();
    }

    private int handleGlobal(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        eggManager.setActive(!eggManager.isActive());

        if (eggManager.isActive()) {
            executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.toggle.enabled")));
        } else {
            executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.toggle.disabled")));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int handlePlayer(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        final List<Player> targets;
        try {
            targets = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
        } catch (CommandSyntaxException e) {
            executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.error.unknown_user")));
            return Command.SINGLE_SUCCESS;
        }

        if (targets.isEmpty()) {
            executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.error.unknown_user")));
            return Command.SINGLE_SUCCESS;
        }

        final List<Component> added = new ArrayList<>();
        final List<Component> removed = new ArrayList<>();

        for (final Player target : targets) {
            if (eggManager.getBlacklist().contains(target.getUniqueId())) {
                eggManager.getBlacklist().remove(target.getUniqueId());
                removed.add(target.displayName());
            } else {
                eggManager.getBlacklist().add(target.getUniqueId());
                added.add(target.displayName());
            }
        }

        if (!removed.isEmpty()) {
            executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.toggle.removed",
                    Argument.component("name", Component.join(JoinConfiguration.commas(true), removed)))));
        }

        if (!added.isEmpty()) {
            executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.toggle.added",
                    Argument.component("name", Component.join(JoinConfiguration.commas(true), added)))));
        }

        return Command.SINGLE_SUCCESS;
    }
}
