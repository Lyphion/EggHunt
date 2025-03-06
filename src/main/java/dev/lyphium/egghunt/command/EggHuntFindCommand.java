package dev.lyphium.egghunt.command;

import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import dev.lyphium.egghunt.util.PermissionConstants;
import dev.lyphium.egghunt.util.TextConstants;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public final class EggHuntFindCommand implements SubCommand {

    private final ResourceManager resourceManager;

    public EggHuntFindCommand(@NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public String getMinimumPermission() {
        return PermissionConstants.ADMIN;
    }

    @Override
    public boolean handleCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextConstants.PREFIX.append(Component.translatable("command.egghunt.error.only_player", ColorConstants.WARNING)));
            return true;
        }

        if (args.length != 0) {
            return false;
        }

        int range = resourceManager.getMaximumRange();
        final List<Item> items = player.getWorld().getNearbyEntitiesByType(Item.class, player.getLocation(), range)
                .stream()
                .filter(i -> i.getItemStack().getItemMeta().getPersistentDataContainer().has(NamespacedKeyConstants.NATURAL_EGG_KEY))
                .toList();

        for (final Item item : items) {
            player.spawnParticle(Particle.WITCH, item.getLocation().add(0, 2.5, 0), 100, 0, 2.5, 0, 0.01);
        }

        int amount = items.size();
        final Component msg;
        switch (amount) {
            case 0 -> msg = Component.translatable("command.egghunt.find.found.zero", ColorConstants.DEFAULT);
            case 1 -> msg = Component.translatable("command.egghunt.find.found.one", ColorConstants.DEFAULT, Component.text(amount, ColorConstants.HIGHLIGHT));
            default -> msg = Component.translatable("command.egghunt.find.found.multiple", ColorConstants.DEFAULT, Component.text(amount, ColorConstants.HIGHLIGHT));
        }

        sender.sendMessage(TextConstants.PREFIX.append(msg));
        return true;
    }

    @Override
    public List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
