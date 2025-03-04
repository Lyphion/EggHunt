package dev.lyphium.egghunt.data;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class EasterEggDrop {

    private final ItemStack itemDrop;

    private final int minimumAmount, maximumAmount;

    private final int weight;

    @Nullable
    private final String commandDrop;

    public EasterEggDrop(@NotNull ItemStack itemDrop, int minimumAmount, int maximumAmount, int weight) {
        this.itemDrop = itemDrop;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.weight = weight;
        this.commandDrop = null;
    }

    public EasterEggDrop(@NotNull String commandDrop, int weight) {
        this.itemDrop = ItemStack.empty();
        this.minimumAmount = 0;
        this.maximumAmount = 0;
        this.weight = weight;
        this.commandDrop = commandDrop;
    }
}
