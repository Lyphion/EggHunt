package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.manager.EggManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EggHuntSpawnCommand implements SubCommand {

    private final EggManager eggManager;

    public EggHuntSpawnCommand(@NotNull EggManager eggManager) {
        this.eggManager = eggManager;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return false;
    }

    @Override
    public @NotNull List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
