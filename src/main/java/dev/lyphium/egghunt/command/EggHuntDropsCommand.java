package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.inventory.DropsInventory;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class EggHuntDropsCommand implements SubCommand {

    private final ResourceManager resourceManager;

    public EggHuntDropsCommand(@NotNull ResourceManager resourceManager) {
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

        // Open overview if no other arguments are provided
        if (args.length == 0) {
            player.openInventory(new DropsInventory(resourceManager, player.locale()).getInventory());
            return true;
        }

        // Check if arguments have the right amount of members
        if (args.length < 3)
            return false;

        // Currently only adding is supported
        if (!args[0].equalsIgnoreCase("add"))
            return false;

        // Check if an item should be added
        if (args[1].equalsIgnoreCase("item")) {
            // Check if arguments have the right amount of members
            if (args.length != 5)
                return false;

            // Check if provided numbers are valid
            if (!args[2].matches("\\d+") || !args[3].matches("\\d+") || !args[4].matches("\\d+")) {
                sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.drops.invalid_format", ColorConstants.ERROR)));
                return true;
            }

            // Check if item in hand is valid
            final ItemStack item = player.getInventory().getItemInMainHand().asOne();
            if (item.getType() == Material.AIR) {
                sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.drops.invalid_item", ColorConstants.ERROR)));
                return true;
            }

            final int minimum = Integer.parseUnsignedInt(args[2]);
            final int maximum = Integer.parseUnsignedInt(args[3]);

            // Check if range is valid
            if (minimum > maximum) {
                sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.drops.invalid_range", ColorConstants.ERROR)));
                return true;
            }

            final int weight = Integer.parseUnsignedInt(args[4]);

            // Create and save drop
            final EasterEggDrop drop = new EasterEggDrop(item, minimum, maximum, weight);
            resourceManager.addDrop(drop);

            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.drops.success", ColorConstants.SUCCESS)));
        } else if (args[1].equalsIgnoreCase("command")) {
            // Check if arguments have the right amount of members
            if (args.length < 4)
                return false;

            // Check if provided number are valid
            if (!args[args.length - 1].matches("\\d+")) {
                sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.drops.wrong_format", ColorConstants.ERROR)));
                return true;
            }

            // Build string from parts
            final String[] commandParts = Arrays.copyOfRange(args, 2, args.length - 1);
            String command = String.join(" ", commandParts);
            if (command.startsWith("\"")) {
                command = command.substring(1);
                if (command.endsWith("\""))
                    command = command.substring(0, command.length() - 1);
            }

            final int weight = Integer.parseUnsignedInt(args[args.length - 1]);

            // Create and save drop
            final EasterEggDrop drop = new EasterEggDrop(command, weight);
            resourceManager.addDrop(drop);

            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.drops.success", ColorConstants.SUCCESS)));
        } else {
            return false;
        }

        return true;
    }

    @Override
    public List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        final String name = args[0].toLowerCase();

        return switch (args.length) {
            case 1 -> Stream.of("add").filter(s -> s.startsWith(name)).toList();
            case 2 -> {
                if (!name.equals("add"))
                    yield List.of();

                final String category = args[1].toLowerCase();
                yield Stream.of("item", "command").filter(s -> s.startsWith(category)).toList();
            }
            default -> List.of();
        };
    }
}
