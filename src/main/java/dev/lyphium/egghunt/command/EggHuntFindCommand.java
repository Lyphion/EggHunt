package dev.lyphium.egghunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage", "SameReturnValue"})
public final class EggHuntFindCommand implements SubCommand {

    private final ResourceManager resourceManager;

    @Getter
    private final String minimumPermission = PermissionConstants.FIND;

    @Getter
    private final String name = "find";

    public EggHuntFindCommand(@NotNull ResourceManager resourceManager) {
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

        // Find all nearby eggs
        int range = resourceManager.getMaximumRange();
        final List<Entity> items = player.getWorld().getNearbyEntitiesByType(Entity.class, player.getLocation(), range)
                .stream()
                .filter(i -> i.getPersistentDataContainer().has(NamespacedKeyConstants.NATURAL_EGG_KEY))
                .toList();

        // Spawn particle above eggs
        for (final Entity entity : items) {
            if (entity.getPersistentDataContainer().has(NamespacedKeyConstants.FAKE_EGG_KEY)) {
                player.spawnParticle(Particle.WITCH, entity.getLocation().add(0, 2.5, 0), 100, 0, 2, 0, 0);
            } else {
                player.spawnParticle(Particle.HAPPY_VILLAGER, entity.getLocation().add(0, 2.5, 0), 100, 0, 2, 0, 0);
            }
        }

        int amount = items.size();
        final Component msg;
        switch (amount) {
            case 0 -> msg = Component.translatable("egghunt.commands.find.found.zero");
            case 1 -> msg = Component.translatable("egghunt.commands.find.found.one", Argument.numeric("amount", amount));
            default -> msg = Component.translatable("egghunt.commands.find.found.multiple", Argument.numeric("amount", amount));
        }

        executor.sendMessage(TextConstants.PREFIX.append(msg));

        return Command.SINGLE_SUCCESS;
    }
}
