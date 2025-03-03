package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EggHuntHelpCommand implements SubCommand {

    private final EggHuntCommand parent;

    public EggHuntHelpCommand(EggHuntCommand parent) {
        this.parent = parent;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length > 0) {
            return false;
        }

        sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.help.menu", ColorConstants.DEFAULT)));

        for (final String command : parent.getSubCommands().keySet()) {
            final TextComponent.Builder builder = Component.text()
                    .content("» ").color(ColorConstants.DEFAULT)
                    .append(Component.text(command, ColorConstants.HIGHLIGHT).clickEvent(ClickEvent.suggestCommand("/egghunt " + command)))
                    .append(Component.text(": ", ColorConstants.DEFAULT))
                    .append(Component.translatable("command.egghunt." + command + ".description", ColorConstants.WHITE));

            sender.sendMessage(builder.build());
        }

        return true;
    }

    @Override
    public List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull[] args) {
        return List.of();
    }
}
