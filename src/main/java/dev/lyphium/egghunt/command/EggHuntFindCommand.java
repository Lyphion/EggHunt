package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.manager.EggManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EggHuntFindCommand implements SubCommand {

    private final EggManager eggManager;

    public EggHuntFindCommand(@NotNull EggManager eggManager) {
        this.eggManager = eggManager;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return false;
    }

    @Override
    public @Nullable List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
