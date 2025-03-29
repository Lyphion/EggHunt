package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class EggHuntFakeCommand implements SubCommand {

    private final ResourceManager resourceManager;

    public EggHuntFakeCommand(@NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public String getMinimumPermission() {
        return PermissionConstants.ADMIN;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        // This command can only be used by players if no arguments are provided
        if (!(sender instanceof Player) && args.length != 1) {
            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.error.only_player", ColorConstants.WARNING)));
            return false;
        }

        // Check if arguments have the right amount of members
        if (args.length > 1)
            return false;

        // Get target player, either self or provided one
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

        target.playSound(resourceManager.getSpawnSound());
        final String format = Objects.requireNonNull(GlobalTranslator.translator().translate("spawn.egg", target.locale())).format(null);
        target.sendActionBar(MiniMessage.miniMessage().deserialize(format));

        sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.fake.success", ColorConstants.SUCCESS)));

        return true;
    }

    @Override
    public @Nullable List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return args.length == 1 ? null : List.of();
    }
}
