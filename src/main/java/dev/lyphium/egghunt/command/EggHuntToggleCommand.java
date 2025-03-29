package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        // Check if arguments have the right amount of members
        if (args.length > 1)
            return false;

        if (args.length == 0) {
            eggManager.setActive(!eggManager.isActive());

            if (eggManager.isActive()) {
                sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.toggle.enabled", ColorConstants.SUCCESS)));
            } else {
                sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.toggle.disabled", ColorConstants.ERROR)));
            }

            return true;
        }

        // Get target player, either self or provided one
        final Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.error.unknown_user", ColorConstants.WARNING,
                    Component.text(args[0], ColorConstants.HIGHLIGHT))));
            return true;
        }

        if (eggManager.getBlacklist().contains(target.getUniqueId())) {
            eggManager.getBlacklist().remove(target.getUniqueId());
            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.toggle.removed", ColorConstants.SUCCESS, target.displayName().color(ColorConstants.HIGHLIGHT))));
        } else {
            eggManager.getBlacklist().add(target.getUniqueId());
            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.toggle.added", ColorConstants.ERROR, target.displayName().color(ColorConstants.HIGHLIGHT))));
        }

        return true;
    }

    @Override
    public List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return args.length == 1 ? null : List.of();
    }
}
