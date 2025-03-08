package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.manager.StatisticManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Main command for managing the plugin.
 */
public final class EggHuntCommand implements CommandExecutor, TabCompleter {

    /**
     * Collection of all available sub commands.
     */
    @Getter(AccessLevel.PACKAGE)
    private final Map<String, SubCommand> subCommands;

    public EggHuntCommand(
            @NotNull JavaPlugin plugin,
            @NotNull ResourceManager resourceManager,
            @NotNull EggManager eggManager,
            @NotNull StatisticManager statisticManager
    ) {
        this.subCommands = new TreeMap<>(Map.of(
                "drops", new EggHuntDropsCommand(resourceManager),
                "find", new EggHuntFindCommand(resourceManager),
                "help", new EggHuntHelpCommand(this),
                "leaderboard", new EggHuntLeaderboardCommand(plugin, resourceManager, statisticManager),
                "models", new EggHuntModelsCommand(resourceManager),
                "reload", new EggHuntReloadCommand(plugin, resourceManager, eggManager),
                "spawn", new EggHuntSpawnCommand(eggManager),
                "toggle", new EggHuntToggleCommand(eggManager)
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

        // Check if user has permission
        if (subCommand.getMinimumPermission() != null && !sender.hasPermission(subCommand.getMinimumPermission())) {
            sender.sendMessage(errorMessage(sender, "help"));
            return true;
        }

        // Run subcommand
        final String[] remaining = Arrays.copyOfRange(args, 1, args.length);
        final boolean success = subCommand.handleCommand(sender, remaining);

        // If something bad happened, print error/help message for command
        if (!success) {
            sender.sendMessage(errorMessage(sender, name));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        final String name = args[0].toLowerCase();

        // Find matching subcommands
        if (args.length == 1) {
            return subCommands.entrySet()
                    .stream()
                    .filter(s -> s.getKey().startsWith(name))
                    .filter(s -> s.getValue().getMinimumPermission() == null || sender.hasPermission(s.getValue().getMinimumPermission()))
                    .map(Map.Entry::getKey)
                    .toList();
        }

        // Check if sub command exists
        if (!subCommands.containsKey(name)) {
            return List.of();
        }

        // Get completion from sub command
        final SubCommand subCommand = subCommands.get(name);
        final String[] remaining = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.handleTabComplete(sender, remaining);
    }

    /**
     * Set this object as an executor and tab completer for the command
     *
     * @param command Command to be handled.
     */
    public void register(@NotNull PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    /**
     * Get error/help message for the command.
     *
     * @param sender Sender of the command
     * @param label  Used label of the command
     * @return Component with formated message.
     */
    private Component errorMessage(@NotNull CommandSender sender, @NotNull String label) {
        final String name = label.toLowerCase();
        final String key = sender.hasPermission(PermissionConstants.ADMIN) && !name.equals("help")
                ? "command.egghunt." + name + ".usage.admin"
                : "command.egghunt." + name + ".usage";

        return TextConstants.PREFIX
                .append(Component.translatable("command.egghunt.error.wrong_usage", ColorConstants.ERROR, Component.translatable(key)));
    }

}
