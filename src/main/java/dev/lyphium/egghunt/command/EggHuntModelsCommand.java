package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.inventory.EasterEggInventory;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EggHuntModelsCommand implements SubCommand {

    private final ResourceManager resourceManager;

    public EggHuntModelsCommand(@NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public String getMinimumPermission() {
        return PermissionConstants.ADMIN;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        // This command can only be used by players
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.error.only_player", ColorConstants.WARNING)));
            return true;
        }

        // Check if arguments have the right amount of members
        if (args.length != 0)
            return false;

        player.openInventory(new EasterEggInventory(resourceManager, player.locale()).getInventory());
        return true;
    }

    @Override
    public List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
