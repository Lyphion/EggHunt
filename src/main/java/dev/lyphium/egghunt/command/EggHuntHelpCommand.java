package dev.lyphium.egghunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.TextConstants;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

@SuppressWarnings({"UnstableApiUsage", "SameReturnValue"})
public final class EggHuntHelpCommand implements SubCommand {

    private final EggHuntCommand parent;

    @Getter
    private final String name = "help";

    public EggHuntHelpCommand(EggHuntCommand parent) {
        this.parent = parent;
    }

    public LiteralCommandNode<CommandSourceStack> construct() {
        return Commands.literal(name)
                .executes(this::handle)
                .build();
    }

    private int handle(CommandContext<CommandSourceStack> ctx) {
        final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

        executor.sendMessage(TextConstants.PREFIX.append(Component.translatable("egghunt.commands.help.menu")));

        // Format all sub commands, and filter with missing permission
        for (final SubCommand command : parent.getSubCommands()) {
            if (command.getMinimumPermission() != null && !executor.hasPermission(command.getMinimumPermission()))
                continue;

            final TextComponent.Builder builder = Component.text()
                    .content("» ").color(NamedTextColor.GRAY)
                    .append(Component.text(command.getName(), ColorConstants.HIGHLIGHT).clickEvent(ClickEvent.suggestCommand("/egghunt " + command.getName())))
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(Component.translatable("egghunt.commands." + command.getName() + ".description", NamedTextColor.WHITE));

            executor.sendMessage(builder.build());
        }

        return Command.SINGLE_SUCCESS;
    }
}
