package dev.lyphium.egghunt.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SubCommand {

    boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args);

    @Nullable List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args);

}
