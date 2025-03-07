package dev.lyphium.egghunt.data;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
public final class EasterEggDrop {

    private final UUID uuid = UUID.randomUUID();

    @Nullable
    private final ItemStack itemDrop;

    private final int minimumAmount, maximumAmount;

    @Nullable
    private final String commandDrop;

    private final int weight;

    public EasterEggDrop(@NotNull ItemStack itemDrop, int minimumAmount, int maximumAmount, int weight) {
        this.itemDrop = itemDrop;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.weight = Math.max(1, weight);
        this.commandDrop = null;
    }

    public EasterEggDrop(@NotNull String commandDrop, int weight) {
        this.itemDrop = null;
        this.minimumAmount = this.maximumAmount = 0;
        this.weight = Math.max(1, weight);
        this.commandDrop = commandDrop;
    }

    public static String getFormatCommand(@NotNull String command, @NotNull Player player) {
        final Random random = new Random();
        final List<? extends Player> players = Bukkit.getOnlinePlayers().stream().toList();
        final Player randomPlayer = players.get(random.nextInt(players.size()));

        Player target = player;
        if (players.size() > 1) {
            final Location location = player.getLocation();

            double distanceSquared = Double.MAX_VALUE;
            for (final Player other : players) {
                if (other == player)
                    continue;

                double temp = other.getLocation().distanceSquared(location);
                if (temp < distanceSquared) {
                    target = other;
                    distanceSquared = temp;
                }
            }
        }

        /*
         * Values to replace
         * @p -> Player opening the egg
         * @r -> Random online player
         * @n -> Nearest other player
         */

        return command
                .replace(" @p ", ' ' + player.getName() + ' ')
                .replace(" @r ", ' ' + randomPlayer.getName() + ' ')
                .replace(" @n ", ' ' + target.getName() + ' ');
    }
}
