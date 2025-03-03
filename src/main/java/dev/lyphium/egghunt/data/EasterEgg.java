package dev.lyphium.egghunt.data;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public final class EasterEgg {

    private final UUID target;

    private final Entity entity;

    @Nullable
    private final ItemStack itemDrop;

    @Nullable
    private final String commandDrop;

    private final LocalDateTime vanishTime;

    public EasterEgg(@NotNull UUID target, @NotNull Entity entity, @NotNull ItemStack itemDrop, @NotNull LocalDateTime vanishTime) {
        this.target = target;
        this.entity = entity;
        this.itemDrop = itemDrop;
        this.commandDrop = null;
        this.vanishTime = vanishTime;
    }

    public EasterEgg(@NotNull UUID target, @NotNull Entity entity, @NotNull String commandDrop, @NotNull LocalDateTime vanishTime) {
        this.target = target;
        this.entity = entity;
        this.itemDrop = null;
        this.commandDrop = commandDrop;
        this.vanishTime = vanishTime;
    }
}
