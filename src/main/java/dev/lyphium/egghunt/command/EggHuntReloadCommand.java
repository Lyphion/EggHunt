package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.EggHunt;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;

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

            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.reload.success", ColorConstants.SUCCESS)));
        } catch (Exception e) {
            EggHunt.getPluginLogger().log(Level.SEVERE, "Failed to reload configurations", e);
            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.reload.failure", ColorConstants.ERROR)));
        }

        return true;
    }

    @Override
    public List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
