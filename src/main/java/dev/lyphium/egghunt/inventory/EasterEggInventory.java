package dev.lyphium.egghunt.inventory;

import dev.lyphium.egghunt.manager.EggManager;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
        this.inventory = Bukkit.createInventory(this, PAGE_SIZE + 9, Component.translatable("inventory.eggs.title", ColorConstants.ERROR));

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

            // Modify item to include description
            item.editMeta(meta -> {
                final List<Component> lore = meta.hasLore() ? Objects.requireNonNull(meta.lore()) : new ArrayList<>();

                if (!lore.isEmpty())
                    lore.add(Component.empty());

                // Add description on how to delete it
                lore.add(GlobalTranslator.render(Component.translatable("inventory.eggs.delete", ColorConstants.DEFAULT, Component.keybind("key.drop"))
                        .decoration(TextDecoration.ITALIC, false), locale));
                meta.lore(lore);
            });

            inventory.setItem(i - startIndex, item);
        }

        // Add page switching items
        final ItemStack previous = new ItemStack(Material.ARROW);
        previous.editMeta(meta -> meta.displayName(Component.translatable("spectatorMenu.previous_page", ColorConstants.DEFAULT).decoration(TextDecoration.ITALIC, false)));
        inventory.setItem(PAGE_SIZE, previous);

        final ItemStack next = new ItemStack(Material.ARROW);
        next.editMeta(meta -> meta.displayName(Component.translatable("spectatorMenu.next_page", ColorConstants.DEFAULT).decoration(TextDecoration.ITALIC, false)));
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
        item.editMeta(meta -> {
            // Setup tag to identity item as egg
            final PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(NamespacedKeyConstants.EASTER_EGG_KEY, PersistentDataType.BOOLEAN, true);

            // Cleanup description from item
            final List<Component> lore = Objects.requireNonNull(meta.lore());
            lore.set(lore.size() - 1, EggManager.USAGE_DESCRIPTION);
            meta.lore(lore);
        });

        return item;
    }

    /**
     * Add a new egg to the pool.
     *
     * @param item Item to be added
     */
    public void addEgg(@NotNull ItemStack item) {
        item = item.asOne();
        item.editMeta(meta -> {
            // Cleanup possible tags
            final PersistentDataContainer container = meta.getPersistentDataContainer();
            container.remove(NamespacedKeyConstants.EASTER_EGG_KEY);

            // Remove description from item
            final List<Component> lore = meta.hasLore() ? Objects.requireNonNull(meta.lore()) : new ArrayList<>();
            lore.remove(EggManager.USAGE_DESCRIPTION);
            if (!lore.isEmpty() && lore.getLast().equals(Component.empty()))
                lore.removeLast();

            meta.lore(lore);
        });

        // Add egg
        resourceManager.getEggs().add(item);
        resourceManager.saveResources();

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

        item.editMeta(meta -> {
            // Remove description from item
            final List<Component> lore = Objects.requireNonNull(meta.lore());
            lore.removeLast();
            if (!lore.isEmpty())
                lore.removeLast();

            meta.lore(lore);
        });

        // Remove egg
        resourceManager.getEggs().remove(item);
        resourceManager.saveResources();

        setupInventory();
        return item;
    }
}
