package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public final class EggHuntHelpCommand implements SubCommand {

    private final EggHuntCommand parent;

    public EggHuntHelpCommand(EggHuntCommand parent) {
        this.parent = parent;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        // Check if arguments have the right amount of members
        if (args.length > 0)
            return false;

        sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.help.menu", ColorConstants.DEFAULT)));

        // Format all sub commands, and filter with missing permission
        for (final Map.Entry<String, SubCommand> entry : parent.getSubCommands().entrySet()) {
            if (entry.getValue().getMinimumPermission() != null && !sender.hasPermission(entry.getValue().getMinimumPermission()))
                continue;

            final TextComponent.Builder builder = Component.text()
                    .content("» ").color(ColorConstants.DEFAULT)
                    .append(Component.text(entry.getKey(), ColorConstants.HIGHLIGHT).clickEvent(ClickEvent.suggestCommand("/egghunt " + entry.getKey())))
                    .append(Component.text(": ", ColorConstants.DEFAULT))
                    .append(Component.translatable("command.egghunt." + entry.getKey() + ".description", ColorConstants.WHITE));

            sender.sendMessage(builder.build());
        }

        return true;
    }

    @Override
    public List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
