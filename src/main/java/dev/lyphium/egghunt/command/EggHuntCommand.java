package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class EggHuntCommand implements CommandExecutor, TabCompleter {

    @Getter(AccessLevel.PACKAGE)
    private final Map<String, SubCommand> subCommands;

    public EggHuntCommand() {
        this.subCommands = new TreeMap<>(Map.of(
                "help", new EggHuntHelpCommand(this)
        ));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // Missing sub command
        if (args.length == 0) {
            sender.sendMessage(errorMessage(sender, "help"));
            return true;
        }

        // Check if sub command exists
        final String name = args[0].toLowerCase();
        if (!subCommands.containsKey(name)) {
            sender.sendMessage(errorMessage(sender, "help"));
            return true;
        }

        final SubCommand subCommand = subCommands.get(name);
        final String[] remaining = Arrays.copyOfRange(args, 1, args.length);
        final boolean success = subCommand.handleCommand(sender, remaining);

        if (!success) {
            sender.sendMessage(errorMessage(sender, name));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        final String name = args[0].toLowerCase();

        if (args.length == 1) {
            return subCommands.keySet()
                    .stream()
                    .filter(s -> s.startsWith(name))
                    .toList();
        }

        // Check if sub command exists
        if (!subCommands.containsKey(name)) {
            return List.of();
        }

        final SubCommand subCommand = subCommands.get(name);
        final String[] remaining = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.handleTabComplete(sender, remaining);
    }

    public void register(@NotNull PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    private Component errorMessage(@NotNull CommandSender sender, @NotNull String label) {
        final String name = label.toLowerCase();
        final String key = sender.hasPermission(PermissionConstants.ADMIN) && !name.equals("help")
                ? "command.egghunt." + name + ".usage.admin"
                : "command.egghunt." + name + ".usage";

        return TextConstants.PREFIX
                .append(Component.translatable("command.egghunt.error.wrong_usage", ColorConstants.ERROR, Component.translatable(key)));
    }

}
