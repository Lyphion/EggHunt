package dev.lyphium.egghunt.inventory;

import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import dev.lyphium.egghunt.util.OneOf;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.persistence.PersistentDataContainerView;
import lombok.Getter;
import net.kyori.adventure.text.Component;
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

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
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

                // Add tag to enable easy deletion
                item.editPersistentDataContainer(container -> container.set(NamespacedKeyConstants.DROP_ID_KEY, PersistentDataType.STRING, drop.getUuid().toString()));

                // Modify item to include description
                final List<Component> lore = item.hasData(DataComponentTypes.LORE)
                        ? new ArrayList<>(Objects.requireNonNull(item.getData(DataComponentTypes.LORE)).lines())
                        : new ArrayList<>();

                if (!lore.isEmpty())
                    lore.add(Component.empty());

                // Add description of amount and weight
                lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.amount", ColorConstants.DEFAULT,
                                Component.text(drop.getMinimumAmount(), ColorConstants.ERROR), Component.text(drop.getMaximumAmount(), ColorConstants.ERROR))
                        .decoration(TextDecoration.ITALIC, false), locale));
                lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.weight", ColorConstants.DEFAULT,
                                Component.text(drop.getWeight(), ColorConstants.ERROR))
                        .decoration(TextDecoration.ITALIC, false), locale));

                final String probability = String.format(Locale.ENGLISH, "%.3f", 100.0 * drop.getWeight() / resourceManager.getTotalWeight());
                lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.probability", ColorConstants.DEFAULT,
                                Component.text(probability, ColorConstants.HIGHLIGHT))
                        .decoration(TextDecoration.ITALIC, false), locale));

                lore.add(Component.empty());

                // Add description on how to delete it
                lore.add(GlobalTranslator.render(Component.translatable("inventory.eggs.delete", ColorConstants.DEFAULT, Component.keybind("key.drop"))
                        .decoration(TextDecoration.ITALIC, false), locale));

                item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

                inventory.setItem(i - startIndex, item);
            } else if (drop.getCommandDrop() != null) {
                final ItemStack item = new ItemStack(Material.PAPER);

                // Add tag to enable easy deletion
                item.editPersistentDataContainer(container -> {
                    container.set(NamespacedKeyConstants.DROP_ID_KEY, PersistentDataType.STRING, drop.getUuid().toString());
                    container.set(NamespacedKeyConstants.COMMAND_DROP_KEY, PersistentDataType.STRING, drop.getCommandDrop());
                });

                item.setData(DataComponentTypes.ITEM_NAME, Component.translatable("advMode.command", ColorConstants.HIGHLIGHT));
                item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

                // Modify item to include description
                final List<Component> lore = new ArrayList<>();

                // Add command as core
                lore.add(Component.text("» ", ColorConstants.DEFAULT).decoration(TextDecoration.ITALIC, false)
                        .append(Component.translatable(drop.getCommandDrop(), ColorConstants.WHITE).decoration(TextDecoration.ITALIC, false)));
                lore.add(Component.empty());

                // Add description of amount and weight
                lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.weight", ColorConstants.DEFAULT,
                                Component.text(drop.getWeight(), ColorConstants.ERROR))
                        .decoration(TextDecoration.ITALIC, false), locale));

                final String probability = String.format(Locale.ENGLISH, "%.3f", 100.0 * drop.getWeight() / resourceManager.getTotalWeight());
                lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.probability", ColorConstants.DEFAULT,
                                Component.text(probability, ColorConstants.HIGHLIGHT))
                        .decoration(TextDecoration.ITALIC, false), locale));

                lore.add(Component.empty());

                // Add description on how to delete it
                lore.add(GlobalTranslator.render(Component.translatable("inventory.eggs.delete", ColorConstants.DEFAULT, Component.keybind("key.drop"))
                        .decoration(TextDecoration.ITALIC, false), locale));

                item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

                inventory.setItem(i - startIndex, item);
            }
        }

        // Add page switching items
        final ItemStack previous = new ItemStack(Material.ARROW);
        previous.setData(DataComponentTypes.ITEM_NAME, Component.translatable("spectatorMenu.previous_page", ColorConstants.DEFAULT));
        inventory.setItem(PAGE_SIZE, previous);

        final ItemStack next = new ItemStack(Material.ARROW);
        next.setData(DataComponentTypes.ITEM_NAME, Component.translatable("spectatorMenu.next_page", ColorConstants.DEFAULT));
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
        final PersistentDataContainerView container = item.getPersistentDataContainer();
        if (container.has(NamespacedKeyConstants.COMMAND_DROP_KEY)) {
            return OneOf.ofSecond(Objects.requireNonNull(container.get(NamespacedKeyConstants.COMMAND_DROP_KEY, PersistentDataType.STRING)));
        }

        // Otherwise cleanup item
        item = item.clone();
        cleanupItem(item);

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
        final PersistentDataContainerView container = item.getPersistentDataContainer();
        final String id = container.get(NamespacedKeyConstants.DROP_ID_KEY, PersistentDataType.STRING);
        final UUID uuid = UUID.fromString(Objects.requireNonNull(id));

        // Remove drop with matching id
        resourceManager.removeDrop(uuid);
        setupInventory();

        // If drop was command, return nothing
        if (container.has(NamespacedKeyConstants.COMMAND_DROP_KEY))
            return null;

        cleanupItem(item);

        return item;
    }

    /**
     * Remove lore and tag from item.
     *
     * @param item Item to clean up
     */
    private void cleanupItem(@NotNull ItemStack item) {
        item.editPersistentDataContainer(c -> c.remove(NamespacedKeyConstants.DROP_ID_KEY));

        final List<Component> lore = new ArrayList<>(Objects.requireNonNull(item.getData(DataComponentTypes.LORE)).lines());
        for (int i = 0; i < 5; i++)
            lore.removeLast();
        if (!lore.isEmpty())
            lore.removeLast();

        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
    }
}
