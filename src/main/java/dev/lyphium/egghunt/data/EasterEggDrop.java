package dev.lyphium.egghunt.data;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class EasterEggDrop {

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
        this.weight = weight;
        this.commandDrop = null;
    }

    public EasterEggDrop(@NotNull String commandDrop, int weight) {
        this.itemDrop = null;
        this.minimumAmount = this.maximumAmount = 0;
        this.weight = weight;
        this.commandDrop = commandDrop;
    }
}
