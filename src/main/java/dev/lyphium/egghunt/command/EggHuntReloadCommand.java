package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.PermissionConstants;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EggHuntReloadCommand implements SubCommand {

    private final ResourceManager resourceManager;

    public EggHuntReloadCommand(@NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public String getMinimumPermission() {
        return PermissionConstants.ADMIN;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 0) {
            return false;
        }

        try {
            resourceManager.loadResources();
            // TODO Print success message
        } catch (Exception e) {
            // TODO Print error message
        }

        return true;
    }

    @Override
    public List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
