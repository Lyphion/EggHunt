package dev.lyphium.egghunt.inventory;

import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class EasterEggInventory implements InventoryHolder {

    private static final int PAGE_SIZE = 45;

    @Getter
    private final Inventory inventory;
    private final ResourceManager resourceManager;
    private final Locale locale;

    private int page;

    public EasterEggInventory(@NotNull ResourceManager resourceManager, @NotNull Locale locale) {
        this.resourceManager = resourceManager;
        this.locale = locale;
        this.inventory = Bukkit.createInventory(this, PAGE_SIZE + 9, Component.translatable("egghunt.inventory.eggs.title"));

        setupInventory();
    }

    /**
     * Change page of eggs.
     *
     * @param delta Amount of paged to switch.
     */
    public void changePage(int delta) {
        int newPage = page + delta;
        // Page can't be negative
        if (newPage < 0)
            newPage = 0;

        // Page can't exceed limit
        final int maxPages = resourceManager.getEggs().isEmpty() ? 0 : (resourceManager.getEggs().size() - 1) / PAGE_SIZE;
        if (newPage > maxPages)
            newPage = maxPages;

        page = newPage;
        setupInventory();
    }

    /**
     * Setup inventory with items.
     */
    private void setupInventory() {
        final List<ItemStack> eggs = resourceManager.getEggs();

        // Calculate bounds of items
        int startIndex = page * PAGE_SIZE;
        int endIndex = Math.min((page + 1) * PAGE_SIZE, eggs.size());

        inventory.clear();
        for (int i = startIndex; i < endIndex; i++) {
            final ItemStack item = eggs.get(i).clone();

            final List<Component> lore = item.hasData(DataComponentTypes.LORE)
                    ? new ArrayList<>(Objects.requireNonNull(item.getData(DataComponentTypes.LORE)).lines())
                    : new ArrayList<>();

            if (!lore.isEmpty())
                lore.add(Component.empty());

            // Add description on how to delete it
            lore.add(GlobalTranslator.render(Component.translatable("egghunt.inventory.eggs.delete")
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE), locale));

            item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

            inventory.setItem(i - startIndex, item);
        }

        // Add page switching items
        final ItemStack previous = new ItemStack(Material.ARROW);
        previous.setData(DataComponentTypes.ITEM_NAME, Component.translatable("spectatorMenu.previous_page", NamedTextColor.GRAY));
        inventory.setItem(PAGE_SIZE, previous);

        final ItemStack next = new ItemStack(Material.ARROW);
        next.setData(DataComponentTypes.ITEM_NAME, Component.translatable("spectatorMenu.next_page", NamedTextColor.GRAY));
        inventory.setItem(PAGE_SIZE + 8, next);
    }

    /**
     * Get the item at the slot.
     *
     * @param slot Slot if the egg
     * @return Item from the slot.
     */
    public @Nullable ItemStack getEgg(int slot) {
        ItemStack item = inventory.getItem(slot);
        if (item == null || slot < 0 || slot >= PAGE_SIZE)
            return null;

        item = item.clone();
        item.editPersistentDataContainer(container -> {
            // Add tags identifying the egg
            container.set(NamespacedKeyConstants.EASTER_EGG_KEY, PersistentDataType.BOOLEAN, true);
        });

        final List<Component> lore = new ArrayList<>(Objects.requireNonNull(item.getData(DataComponentTypes.LORE)).lines());

        // Cleanup description from item
        lore.set(lore.size() - 1, EggManager.USAGE_DESCRIPTION);
        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

        return item;
    }

    /**
     * Add a new egg to the pool.
     *
     * @param item Item to be added
     */
    public void addEgg(@NotNull ItemStack item) {
        item = item.asOne();

        item.editPersistentDataContainer(container -> {
            // Cleanup possible tags
            container.remove(NamespacedKeyConstants.EASTER_EGG_KEY);
        });

        // Cleanup description from item
        if (item.hasData(DataComponentTypes.LORE)) {
            final List<Component> lore = new ArrayList<>(Objects.requireNonNull(item.getData(DataComponentTypes.LORE)).lines());

            // Remove description from item
            lore.remove(EggManager.USAGE_DESCRIPTION);
            if (!lore.isEmpty() && lore.getLast().equals(Component.empty()))
                lore.removeLast();

            item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
        }

        // Add egg
        resourceManager.addEgg(item);

        setupInventory();
    }

    /**
     * Remove the egg from the pool.
     *
     * @param slot Slot of the egg
     * @return Item of the egg
     */
    public @Nullable ItemStack removeEgg(int slot) {
        final ItemStack item = inventory.getItem(slot);
        if (item == null || slot < 0 || slot >= PAGE_SIZE)
            return null;

        final List<Component> lore = new ArrayList<>(Objects.requireNonNull(item.getData(DataComponentTypes.LORE)).lines());

        // Remove description from item
        lore.removeLast();
        if (!lore.isEmpty())
            lore.removeLast();

        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

        // Remove egg
        resourceManager.removeEgg(item);

        setupInventory();
        return item;
    }
}
