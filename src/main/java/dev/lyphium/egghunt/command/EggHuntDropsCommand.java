package dev.lyphium.egghunt.command;

import com.google.common.collect.Range;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.inventory.DropsInventory;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.PermissionConstants;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("SameReturnValue")
public final class EggHuntDropsCommand implements SubCommand {

    @Getter
    private final String name = "drops";

    @Getter
    private final String minimumPermission = PermissionConstants.CONFIGURE;

    @Getter
    private final Component description = Component.translatable("egghunt.command.egghunt.drops.description");

    private final ResourceManager resourceManager;

    public EggHuntDropsCommand(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public LiteralCommandNode<CommandSourceStack> construct() {
        return Commands.literal(name)
                .requires(s -> s.getSender().hasPermission(minimumPermission))
                .executes(this::handleInventory)
                .then(Commands.literal("add")
                        .then(Commands.literal("item")
                                .then(Commands.argument("amount", ArgumentTypes.integerRange())
                                        .then(Commands.argument("weight", IntegerArgumentType.integer(1))
                                                .executes(this::handleItem)
                                                .then(Commands.argument("item", ArgumentTypes.itemStack())
                                                        .executes(this::handleInlineItem))
                                        )
                                )
                        )
                        .then(Commands.literal("command")
                                .then(Commands.argument("weight", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                                .executes(this::handleCommand)
                                        )
                                )
                        )
                )
                .build();
    }

    private int handleInventory(CommandContext<CommandSourceStack> ctx) {
        final Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            if (executor != null)
                executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.drops.only_player")));
            return Command.SINGLE_SUCCESS;
        }

        player.openInventory(new DropsInventory(resourceManager, player.locale()).getInventory());
        return Command.SINGLE_SUCCESS;
    }

    private int handleItem(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.drops.only_player")));
            return Command.SINGLE_SUCCESS;
        }

        final Range<Integer> amount = ctx.getArgument("amount", IntegerRangeProvider.class).range();
        final int weight = IntegerArgumentType.getInteger(ctx, "weight");

        if (!amount.hasLowerBound() || !amount.hasUpperBound() || amount.lowerEndpoint() < 1) {
            player.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.drops.invalid_range")));
            return Command.SINGLE_SUCCESS;
        }

        // Check if item in hand is valid
        final ItemStack item = player.getInventory().getItemInMainHand().asOne();
        if (item.isEmpty()) {
            player.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.drops.invalid_item")));
            return Command.SINGLE_SUCCESS;
        }

        final EasterEggDrop drop = new EasterEggDrop(item, amount.lowerEndpoint(), amount.upperEndpoint(), weight);
        resourceManager.addDrop(drop);

        player.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.drops.success")));
        return Command.SINGLE_SUCCESS;
    }

    private int handleInlineItem(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        final Range<Integer> amount = ctx.getArgument("amount", IntegerRangeProvider.class).range();
        final int weight = IntegerArgumentType.getInteger(ctx, "weight");

        if (!amount.hasLowerBound() || !amount.hasUpperBound() || amount.lowerEndpoint() < 1) {
            executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.drops.invalid_range")));
            return Command.SINGLE_SUCCESS;
        }

        final ItemStack item = ctx.getArgument("item", ItemStack.class);
        if (item.isEmpty()) {
            executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.drops.invalid_item")));
            return Command.SINGLE_SUCCESS;
        }

        final EasterEggDrop drop = new EasterEggDrop(item, amount.lowerEndpoint(), amount.upperEndpoint(), weight);
        resourceManager.addDrop(drop);

        executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.drops.success")));

        return Command.SINGLE_SUCCESS;
    }

    private int handleCommand(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        final int weight = IntegerArgumentType.getInteger(ctx, "weight");
        final String command = StringArgumentType.getString(ctx, "command");

        final EasterEggDrop drop = new EasterEggDrop(command, weight);
        resourceManager.addDrop(drop);

        executor.sendMessage(Component.translatable("egghunt.chat.prefix").append(Component.translatable("egghunt.command.egghunt.drops.success")));
        return Command.SINGLE_SUCCESS;
    }
}
