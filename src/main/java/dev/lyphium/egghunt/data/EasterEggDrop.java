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

/**
 * Easter egg drop with item or command.
 */
@Getter
public final class EasterEggDrop {

    /**
     * Id of the drop, used internally for removal.
     */
    private final UUID uuid = UUID.randomUUID();

    /**
     * Possible item drop of an Easter egg. Only set if {@code commandDrop} is {@code null}.
     */
    @Nullable
    private final ItemStack itemDrop;

    /**
     * Range of how many items can be dropped.
     */
    private final int minimumAmount, maximumAmount;

    /**
     * Possible command drop of an Easter egg. Only set if {@code itemDrop} is {@code null}.
     */
    @Nullable
    private final String commandDrop;

    /**
     * Weight of this drop, compared to others.
     */
    private final int weight;

    /**
     * Create a new Easter egg drop with an item.
     *
     * @param itemDrop      Item to drop
     * @param minimumAmount The minimum amount of the item
     * @param maximumAmount The maximum amount of the item
     * @param weight        Weight of this drop, compared to others
     */
    public EasterEggDrop(@NotNull ItemStack itemDrop, int minimumAmount, int maximumAmount, int weight) {
        this.itemDrop = itemDrop;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.weight = Math.max(1, weight);
        this.commandDrop = null;
    }

    /**
     * Create a new Easter egg drop with a server command.
     *
     * @param commandDrop Command to be executed by the console
     * @param weight      Weight of this drop, compared to others
     */
    public EasterEggDrop(@NotNull String commandDrop, int weight) {
        this.itemDrop = null;
        this.minimumAmount = this.maximumAmount = 0;
        this.weight = Math.max(1, weight);
        this.commandDrop = commandDrop;
    }

    /**
     * Format the command string and replace placeholders
     *
     * @param command Command to format
     * @param player  Player targeting the command
     * @return Formated command.
     */
    public static @NotNull String getFormatCommand(@NotNull String command, @NotNull Player player) {
        final Random random = new Random();
        final List<? extends Player> players = Bukkit.getOnlinePlayers().stream().toList();
        final Player randomPlayer = players.get(random.nextInt(players.size()));

        // Find nearest player or self, if none is available
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
