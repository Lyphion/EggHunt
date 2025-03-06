package dev.lyphium.egghunt.inventory;

import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EasterEggInventory implements InventoryHolder {

    @Getter
    private final Inventory inventory;
    private final ResourceManager resourceManager;

    public EasterEggInventory(@NotNull ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.inventory = Bukkit.createInventory(this, 54, Component.translatable("inventory.eggs.title", ColorConstants.ERROR));

        fillInventory();
    }

    private void fillInventory() {
        final List<ItemStack> eggs = resourceManager.getEggs();

        // TODO Handle more eggs
        for (int i = 0; i < eggs.size() && i < 54; i++) {
            final ItemStack item = eggs.get(i);
            inventory.setItem(i, item);
        }
    }

    public @NotNull ItemStack removeEgg(int slot) {
        return null;
    }

    public void addEgg(@NotNull ItemStack item) {

    }
}
