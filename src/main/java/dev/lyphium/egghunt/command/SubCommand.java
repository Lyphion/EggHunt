package dev.lyphium.egghunt.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;

public interface SubCommand {

    /**
     * Get the name of the sub command.
     *
     * @return Name of the command.
     */
    String getName();

    /**
     * Minimum required permission to run the command. Or {@code null} if no permission is needed.
     *
     * @return Minimum permission to run the command.
     * @implNote Default value is {@code null}.
     */
    default @Nullable String getMinimumPermission() {
        return null;
    }

    /**
     * Get the description of the sub command.
     *
     * @return Description of the command.
     */
    Component getDescription();

    /**
     * Construct the command.
     *
     * @return Constructed command.
     */
    LiteralCommandNode<CommandSourceStack> construct();

    /**
     * Helper method to get the executor of the command.
     *
     * @param ctx Context of the command
     * @return Executor of the command.
     */
    static CommandSender getExecutor(CommandContext<CommandSourceStack> ctx) {
        return ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();
    }

}
