package dev.lyphium.egghunt.inventory;

import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import dev.lyphium.egghunt.util.OneOf;
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

import java.util.*;

public final class DropsInventory implements InventoryHolder {

    private static final int PAGE_SIZE = 45;

    @Getter
    private final Inventory inventory;
    private final ResourceManager resourceManager;
    private final Locale locale;

    private int page;

    public DropsInventory(@NotNull ResourceManager resourceManager, @NotNull Locale locale) {
        this.resourceManager = resourceManager;
        this.locale = locale;
        this.inventory = Bukkit.createInventory(this, PAGE_SIZE + 9, Component.translatable("inventory.drops.title", ColorConstants.ERROR));

        setupInventory();
    }

    /**
     * Change page of drops.
     *
     * @param delta Amount of paged to switch.
     */
    public void changePage(int delta) {
        int newPage = page + delta;
        // Page can't be negative
        if (newPage < 0)
            newPage = 0;

        // Page can't exceed limit
        final int maxPages = resourceManager.getDrops().isEmpty() ? 0 : (resourceManager.getDrops().size() - 1) / PAGE_SIZE;
        if (newPage > maxPages)
            newPage = maxPages;

        page = newPage;
        setupInventory();
    }

    /**
     * Setup inventory with items.
     */
    private void setupInventory() {
        final List<EasterEggDrop> drops = resourceManager.getDrops();

        // Calculate bounds of items
        int startIndex = page * PAGE_SIZE;
        int endIndex = Math.min((page + 1) * PAGE_SIZE, drops.size());

        inventory.clear();
        for (int i = startIndex; i < endIndex; i++) {
            final EasterEggDrop drop = drops.get(i);
            if (drop.getItemDrop() != null) {
                final ItemStack item = drop.getItemDrop().asOne();

                // Modify item to include description
                item.editMeta(meta -> {
                    // Add tag to enable easy deletion
                    final PersistentDataContainer container = meta.getPersistentDataContainer();
                    container.set(NamespacedKeyConstants.DROP_ID_KEY, PersistentDataType.STRING, drop.getUuid().toString());

                    final List<Component> lore = meta.hasLore() ? Objects.requireNonNull(meta.lore()) : new ArrayList<>();
                    if (!lore.isEmpty())
                        lore.add(Component.empty());

                    // Add description of amount and weight
                    lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.amount", ColorConstants.DEFAULT,
                                    Component.text(drop.getMinimumAmount(), ColorConstants.ERROR), Component.text(drop.getMaximumAmount(), ColorConstants.ERROR))
                            .decoration(TextDecoration.ITALIC, false), locale));
                    lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.weight", ColorConstants.DEFAULT,
                                    Component.text(drop.getWeight(), ColorConstants.ERROR))
                            .decoration(TextDecoration.ITALIC, false), locale));

                    // Add description on how to delete it
                    lore.add(GlobalTranslator.render(Component.translatable("inventory.eggs.delete", ColorConstants.DEFAULT, Component.keybind("key.drop"))
                            .decoration(TextDecoration.ITALIC, false), locale));

                    meta.lore(lore);
                });

                inventory.setItem(i - startIndex, item);
            } else if (drop.getCommandDrop() != null) {
                final ItemStack item = new ItemStack(Material.PAPER);

                // Modify item to include description
                item.editMeta(meta -> {
                    // Add tag to enable easy deletion and mark as command
                    final PersistentDataContainer container = meta.getPersistentDataContainer();
                    container.set(NamespacedKeyConstants.DROP_ID_KEY, PersistentDataType.STRING, drop.getUuid().toString());
                    container.set(NamespacedKeyConstants.COMMAND_DROP_KEY, PersistentDataType.STRING, drop.getCommandDrop());

                    // Change display name to "Command"
                    meta.displayName(Component.translatable("advMode.command", ColorConstants.HIGHLIGHT).decoration(TextDecoration.ITALIC, false));
                    meta.setEnchantmentGlintOverride(true);

                    final List<Component> lore = new ArrayList<>();

                    // Add command as core
                    lore.add(Component.text("» ", ColorConstants.DEFAULT).decoration(TextDecoration.ITALIC, false)
                            .append(Component.translatable(drop.getCommandDrop(), ColorConstants.WHITE).decoration(TextDecoration.ITALIC, false)));
                    lore.add(Component.empty());

                    // Add description of amount and weight
                    lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.weight", ColorConstants.DEFAULT,
                                    Component.text(drop.getWeight(), ColorConstants.ERROR))
                            .decoration(TextDecoration.ITALIC, false), locale));

                    // Add description on how to delete it
                    lore.add(GlobalTranslator.render(Component.translatable("inventory.eggs.delete", ColorConstants.DEFAULT, Component.keybind("key.drop"))
                            .decoration(TextDecoration.ITALIC, false), locale));

                    meta.lore(lore);
                });

                inventory.setItem(i - startIndex, item);
            }
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
     * Get the item or command from the drop at the slot.
     *
     * @param slot Slot if the drop
     * @return Item or command with from the slot.
     */
    public @Nullable OneOf<ItemStack, String> getDrop(int slot) {
        ItemStack item = inventory.getItem(slot);
        if (item == null || slot < 0 || slot >= PAGE_SIZE)
            return null;

        // Check if Command, then return command string
        final PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(NamespacedKeyConstants.COMMAND_DROP_KEY)) {
            return OneOf.ofSecond(Objects.requireNonNull(container.get(NamespacedKeyConstants.COMMAND_DROP_KEY, PersistentDataType.STRING)));
        }

        // Otherwise cleanup item
        item = item.clone();
        item.editMeta(meta -> {
            final PersistentDataContainer c = meta.getPersistentDataContainer();
            c.remove(NamespacedKeyConstants.DROP_ID_KEY);

            // Remove description from item
            final List<Component> lore = Objects.requireNonNull(meta.lore());
            lore.removeLast();
            lore.removeLast();
            lore.removeLast();
            if (!lore.isEmpty())
                lore.removeLast();

            meta.lore(lore);
        });

        return OneOf.ofFirst(item);
    }

    /**
     * Remove the drop from the pool.
     *
     * @param slot Slot of the drop
     * @return Item of the drop, or nothing if it was a command.
     */
    public @Nullable ItemStack removeDrop(int slot) {
        ItemStack item = inventory.getItem(slot);
        if (item == null || slot < 0 || slot >= PAGE_SIZE)
            return null;

        // Get id of drop
        final PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        final String id = container.get(NamespacedKeyConstants.DROP_ID_KEY, PersistentDataType.STRING);
        final UUID uuid = UUID.fromString(Objects.requireNonNull(id));

        // Remove drop with matching id
        resourceManager.removeDrop(uuid);
        setupInventory();

        // If drop was command, return nothing
        if (container.has(NamespacedKeyConstants.COMMAND_DROP_KEY)) {
            return null;
        }

        // Otherwise cleanup item
        item.editMeta(meta -> {
            final PersistentDataContainer c = meta.getPersistentDataContainer();
            c.remove(NamespacedKeyConstants.DROP_ID_KEY);

            // Remove description from item
            final List<Component> lore = Objects.requireNonNull(meta.lore());
            lore.removeLast();
            lore.removeLast();
            if (!lore.isEmpty())
                lore.removeLast();

            meta.lore(lore);
        });

        return item;
    }
}
