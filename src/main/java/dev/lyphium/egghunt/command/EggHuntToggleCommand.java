package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EggHuntToggleCommand implements SubCommand {

    private final EggManager eggManager;

    public EggHuntToggleCommand(@NotNull EggManager eggManager) {
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

        eggManager.setActive(!eggManager.isActive());

        if (eggManager.isActive()) {
            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.toggle.enabled", ColorConstants.SUCCESS)));
        } else {
            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.toggle.disabled", ColorConstants.ERROR)));
        }

        return true;
    }

    @Override
    public List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
