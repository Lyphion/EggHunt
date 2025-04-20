package dev.lyphium.egghunt.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import javax.annotation.Nullable;

@SuppressWarnings("UnstableApiUsage")
public interface SubCommand {

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

    LiteralCommandNode<CommandSourceStack> construct();

}
