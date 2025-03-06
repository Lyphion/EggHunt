package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.EggHunt;
import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.nio.Buffer;
import java.util.List;
import java.util.logging.Level;

public final class EggHuntReloadCommand implements SubCommand {

    private final ResourceManager resourceManager;
    private final EggManager eggManager;

    public EggHuntReloadCommand(
            @NotNull ResourceManager resourceManager,
            @NotNull EggManager eggManager
    ) {
        this.resourceManager = resourceManager;
        this.eggManager = eggManager;
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
            Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).forEach(eggManager::resetSpawnTimer);

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
