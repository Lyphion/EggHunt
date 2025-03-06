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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EggHuntSpawnCommand implements SubCommand {

    private final EggManager eggManager;

    public EggHuntSpawnCommand(@NotNull EggManager eggManager) {
        this.eggManager = eggManager;
    }

    @Override
    public String getMinimumPermission() {
        return PermissionConstants.ADMIN;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player) && args.length != 1) {
            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.error.only_player", ColorConstants.WARNING)));
            return false;
        }

        if (args.length > 1) {
            return false;
        }

        final Player target;
        if (args.length == 0) {
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.error.unknown_user", ColorConstants.WARNING,
                        Component.text(args[0], ColorConstants.HIGHLIGHT))));
                return true;
            }
        }

        eggManager.spawn(target.getLocation());
        sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.spawn.success", ColorConstants.SUCCESS)));

        return true;
    }

    @Override
    public @Nullable List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return args.length == 1 ? null : List.of();
    }
}
